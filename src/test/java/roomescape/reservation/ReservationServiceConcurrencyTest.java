package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.reservationwait.ReservationWaitDao;
import roomescape.reservationwait.ReservationWaitService;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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

    private static final String INSERT_JEONGKONG_WAIT_SQL = """
            INSERT INTO reservation_wait (id, reservation_id, member_id)
            VALUES (1, 1, 2);
            """;

    @MockitoSpyBean
    ReservationWaitDao spyWaitDao;

    private final ReservationService reservationService;
    private final ReservationWaitService reservationWaitService;
    private final PlatformTransactionManager txManager;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReservationServiceConcurrencyTest(ReservationService reservationService,
                                             ReservationWaitService reservationWaitService,
                                             PlatformTransactionManager txManager,
                                             JdbcTemplate jdbcTemplate) {
        this.reservationService = reservationService;
        this.reservationWaitService = reservationWaitService;
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
    void 양도_중_대기_취소가_lock_으로_차단되어_유령_양도가_발생하지_않는다() throws Exception {
        // given
        CountDownLatch t1LockAcquired = new CountDownLatch(1);

        // Spy: T1 이 findEarliestMemberIdForUpdate 호출 시
        //   1) 실제 SQL 실행 (FOR UPDATE 로 row lock 획득)
        //   2) T2 에게 "지금 시도해" 신호
        //   3) T2 가 시도/대기할 시간 확보 위해 잠시 hold
        doAnswer(invocation -> {
            Optional<Long> result = (Optional<Long>) invocation.callRealMethod();
            t1LockAcquired.countDown();
            Thread.sleep(300);   // T2 가 lock 대기에 들어갈 시간
            return result;
        }).when(spyWaitDao).findEarliestMemberIdForUpdate(anyLong());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // T1: BROWN 의 예약 취소 → 자동 양도 흐름
        Future<?> f1 = executor.submit(() -> {
            new TransactionTemplate(txManager).execute(status -> {
                reservationService.deleteReservation(RESERVATION_ID, BROWN_ID);
                return null;
            });
        });

        // T2: 정콩이의 대기 취소 — T1 의 lock 잡힌 후 시도
        Future<?> f2 = executor.submit(() -> {
            try {
                t1LockAcquired.await();
                new TransactionTemplate(txManager).execute(status -> {
                    reservationWaitService.deleteReservationWait(RESERVATION_ID, JEONGKONG_ID);
                    return null;
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // when: 두 트랜잭션 완료 대기
        f1.get(5, TimeUnit.SECONDS);
        f2.get(5, TimeUnit.SECONDS);
        executor.shutdown();

        // then: 데이터 일관성 — 정콩이 양도받음 (유령 X)
        Long currentOwner = jdbcTemplate.queryForObject(
                "SELECT member_id FROM reservation WHERE id = ?",
                Long.class, RESERVATION_ID);
        assertThat(currentOwner).isEqualTo(JEONGKONG_ID);

        // then: 대기 row 는 양도 시점에 삭제됨 (T2 의 추가 delete 는 0 rows)
        Integer waitCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?",
                Integer.class, RESERVATION_ID);
        assertThat(waitCount).isZero();
    }
}
