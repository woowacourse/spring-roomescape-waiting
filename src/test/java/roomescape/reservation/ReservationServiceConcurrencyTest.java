package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.auth.Role;
import roomescape.member.Member;
import roomescape.reservationhistory.ReservationHistory;
import roomescape.reservationhistory.ReservationHistoryAction;
import roomescape.reservationhistory.ReservationHistoryDao;
import roomescape.reservationwait.ReservationWaitService;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql("/cleanup.sql")
@SqlMergeMode(MergeMode.MERGE)
public class ReservationServiceConcurrencyTest {

    private static final long BROWN_ID = 1L;
    private static final long JEONGKONG_ID = 2L;
    private static final long RESERVATION_ID = 1L;

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_TWO_MEMBERS_SQL = """
            INSERT INTO member (id, email, password, name, role)
            VALUES (1, 'brown@email.com', 'pw', 'BROWN', 'USER'),
                   (2, 'jeongkong@email.com', 'pw', '정콩이', 'USER');
            """;

    private static final String INSERT_DEFAULT_THEME_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '테마', '설명', 'https://example.com/img.jpg');
            """;

    private static final String INSERT_DEFAULT_TIME_SQL = """
            INSERT INTO reservation_time (id, start_at)
            VALUES (1, '10:00');
            """;

    private static final String INSERT_BROWN_RESERVATION_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-12-01', 1, 1, 1);
            """;

    private static final String INSERT_BROWN_CREATED_HISTORY_SQL = """
            INSERT INTO reservation_history
                (reservation_id, member_id, date, time_id, theme_id, store_id, action, actor_id)
            VALUES (1, 1, '2026-12-01', 1, 1, 1, 'CREATED', 1);
            """;

    private static final String INSERT_JEONGKONG_WAIT_SQL = """
            INSERT INTO reservation_wait (id, reservation_id, member_id)
            VALUES (1, 1, 2);
            """;

    private static final String INSERT_GANGNAM_MANAGER_SQL = """
            INSERT INTO member (id, email, password, name, role, store_id)
            VALUES (10, 'gangnam@email.com', 'pw', '강남매니저', 'MANAGER', 1);
            """;

    private final ReservationService reservationService;
    private final ReservationWaitService reservationWaitService;
    private final ReservationHistoryDao reservationHistoryDao;
    private final PlatformTransactionManager txManager;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReservationServiceConcurrencyTest(ReservationService reservationService,
                                             ReservationWaitService reservationWaitService,
                                             ReservationHistoryDao reservationHistoryDao,
                                             PlatformTransactionManager txManager,
                                             JdbcTemplate jdbcTemplate) {
        this.reservationService = reservationService;
        this.reservationWaitService = reservationWaitService;
        this.reservationHistoryDao = reservationHistoryDao;
        this.txManager = txManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_THEME_SQL,
            INSERT_DEFAULT_TIME_SQL,
            INSERT_BROWN_RESERVATION_SQL,
            INSERT_JEONGKONG_WAIT_SQL
    })
    void 예약_취소_트랜잭션이_커밋되기_전_대기자가_본인_대기를_취소해도_양도가_정상_완료된다() throws Exception {
        // given: 두 트랜잭션을 단계별로 동기화하기 위한 신호
        CountDownLatch promotionDoneBeforeCommit = new CountDownLatch(1);
        CountDownLatch allowPromotionCommit = new CountDownLatch(1);
        CountDownLatch waitCancelStarted = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // when: 사용자 취소 트랜잭션이 양도까지 끝낸 뒤 커밋 직전에 멈춤
            Future<?> promotionFuture = executor.submit(() -> {
                new TransactionTemplate(txManager).execute(status -> {
                    reservationService.deleteReservation(RESERVATION_ID, BROWN_ID);
                    promotionDoneBeforeCommit.countDown();
                    await(allowPromotionCommit);
                    return null;
                });
            });

            assertThat(promotionDoneBeforeCommit.await(2, TimeUnit.SECONDS)).isTrue();

            // when: 대기자가 본인 대기 취소 시도 — 양도 트랜잭션의 wait 락에 막혀 진행 불가
            Future<?> waitCancelFuture = executor.submit(() -> {
                waitCancelStarted.countDown();
                new TransactionTemplate(txManager).execute(status -> {
                    reservationWaitService.deleteReservationWait(RESERVATION_ID, JEONGKONG_ID);
                    return null;
                });
            });

            assertThat(waitCancelStarted.await(2, TimeUnit.SECONDS)).isTrue();
            assertStillBlocked(waitCancelFuture);

            // when: 양도 트랜잭션 커밋 허용 → 두 트랜잭션 완료
            allowPromotionCommit.countDown();
            promotionFuture.get(5, TimeUnit.SECONDS);
            waitCancelFuture.get(5, TimeUnit.SECONDS);
        } finally {
            allowPromotionCommit.countDown();
            executor.shutdownNow();
        }

        // then: 양도가 정상 완료되어 정콩이가 reservation 의 owner
        Long currentOwner = jdbcTemplate.queryForObject(
                "SELECT member_id FROM reservation WHERE id = ?",
                Long.class, RESERVATION_ID);
        assertThat(currentOwner).isEqualTo(JEONGKONG_ID);

        // then: 대기 row 는 양도 시점에 소멸되어 비어 있음
        Integer waitCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?",
                Integer.class, RESERVATION_ID);
        assertThat(waitCount).isZero();
    }

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_GANGNAM_MANAGER_SQL,
            INSERT_DEFAULT_THEME_SQL,
            INSERT_DEFAULT_TIME_SQL,
            INSERT_BROWN_RESERVATION_SQL,
            INSERT_BROWN_CREATED_HISTORY_SQL,
            INSERT_JEONGKONG_WAIT_SQL
    })
    void 사용자와_매니저가_같은_예약을_동시_취소해도_history가_현재_owner_명의로_기록된다() throws Exception {
        // given: 두 트랜잭션을 단계별로 동기화하기 위한 신호
        CountDownLatch userCancelDoneBeforeCommit = new CountDownLatch(1);
        CountDownLatch allowUserCancelCommit = new CountDownLatch(1);
        CountDownLatch managerDeleteStarted = new CountDownLatch(1);

        Member gangnamManager = new Member(
                10L, "gangnam@email.com", "pw", "강남매니저", Role.MANAGER, 1L);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // when: 사용자 취소 트랜잭션이 양도까지 끝낸 뒤 커밋 직전에 멈춤
            Future<?> userCancelFuture = executor.submit(() -> {
                new TransactionTemplate(txManager).execute(status -> {
                    reservationService.deleteReservation(RESERVATION_ID, BROWN_ID);
                    userCancelDoneBeforeCommit.countDown();
                    await(allowUserCancelCommit);
                    return null;
                });
            });

            assertThat(userCancelDoneBeforeCommit.await(2, TimeUnit.SECONDS)).isTrue();

            // when: 매니저 취소 시도 — 사용자 트랜잭션의 reservation 락에 막혀 진행 불가
            Future<?> managerDeleteFuture = executor.submit(() -> {
                managerDeleteStarted.countDown();
                new TransactionTemplate(txManager).execute(status -> {
                    reservationService.deleteReservationByManager(RESERVATION_ID, gangnamManager);
                    return null;
                });
            });

            assertThat(managerDeleteStarted.await(2, TimeUnit.SECONDS)).isTrue();
            assertStillBlocked(managerDeleteFuture);

            // when: 사용자 트랜잭션 커밋 허용 → 매니저는 양도된 최신 owner 스냅샷으로 진행
            allowUserCancelCommit.countDown();
            userCancelFuture.get(5, TimeUnit.SECONDS);
            managerDeleteFuture.get(5, TimeUnit.SECONDS);
        } finally {
            allowUserCancelCommit.countDown();
            executor.shutdownNow();
        }

        // then: 두 트랜잭션 후 reservation 은 소멸
        Integer reservationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?",
                Integer.class, RESERVATION_ID);
        assertThat(reservationCount).isZero();

        // then: 마지막 사건이 옛 owner(BROWN) 가 아닌 양도된 현재 owner(정콩이) 명의로 기록
        List<ReservationHistory> histories = reservationHistoryDao.findByReservationId(RESERVATION_ID);
        ReservationHistory last = histories.get(histories.size() - 1);
        assertThat(last.getAction()).isEqualTo(ReservationHistoryAction.CANCELED);
        assertThat(last.getMemberId()).isEqualTo(JEONGKONG_ID);
    }

    private void await(CountDownLatch latch) {
        try {
            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private void assertStillBlocked(Future<?> future) {
        assertThatThrownBy(() -> future.get(200, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
    }
}
