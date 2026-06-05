package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationTimeCreateCommand;
import roomescape.service.dto.ThemeCreateCommand;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationConcurrencyTest {

    private static final String 매트 = "매트";
    private static final String 루드비코 = "루드비코";
    private static final LocalDate 예약_날짜 = LocalDate.of(2099, 12, 31);

    @Autowired
    private AdminReservationService adminReservationService;
    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ThemeService themeService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long time1Id;
    private Long time2Id;
    private Long time3Id;
    private Long themeId;

    private Long mattReservationId;
    private Long rudevicoReservationId;

    @BeforeEach
    void setUp() {
        time1Id = reservationTimeService.create(new ReservationTimeCreateCommand(LocalTime.of(10, 0))).id();
        time2Id = reservationTimeService.create(new ReservationTimeCreateCommand(LocalTime.of(11, 0))).id();
        time3Id = reservationTimeService.create(new ReservationTimeCreateCommand(LocalTime.of(12, 0))).id();

        themeId = themeService.create(new ThemeCreateCommand("테스트 테마", "설명", "url")).id();

        슬롯_생성(예약_날짜, time1Id, themeId);
        슬롯_생성(예약_날짜, time2Id, themeId);
        슬롯_생성(예약_날짜, time3Id, themeId);

        mattReservationId = 예약_생성(new ReservationCreateCommand(매트, 예약_날짜, time1Id, themeId)).id();
        rudevicoReservationId = 예약_생성(
                new ReservationCreateCommand(루드비코, 예약_날짜, time2Id, themeId)).id();
    }

    @DisplayName("동시에 동일한 사용자가 예약을 생성할 때 중복 생성이 차단된다")
    @Test
    void 동일한_예약_동시_생성시_하나만_성공한다() throws InterruptedException {
        ReservationCreateCommand command = new ReservationCreateCommand(매트, 예약_날짜, time3Id, themeId);

        ConcurrencyResult result = 동시_실행(10, () -> 예약_생성(command));

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(9);
        assertThat(특정_시간의_예약_개수_조회(time3Id)).isEqualTo(1);
    }

    @DisplayName("동시에 서로 다른 사용자가 같은 시간에 예약하면 모두 성공하고 대기 순번이 부여된다")
    @Test
    void 서로_다른_사용자가_동시_예약시_모두_성공하고_대기순번이_부여된다() throws InterruptedException {
        int threadCount = 10;
        Runnable[] actions = new Runnable[threadCount];
        for (int i = 0; i < threadCount; i++) {
            String userName = "사용자" + i;
            ReservationCreateCommand command = new ReservationCreateCommand(userName, 예약_날짜, time3Id, themeId);
            actions[i] = () -> 예약_생성(command);
        }

        ConcurrencyResult result = 동시_실행(actions);

        assertThat(result.successCount()).isEqualTo(threadCount);
        assertThat(result.failCount()).isZero();
        assertThat(특정_시간의_예약_개수_조회(time3Id)).isEqualTo(threadCount);
    }

    @DisplayName("예약 생성과 슬롯 삭제가 동시에 일어날 때 DB 외래키 제약조건에 의해 둘 중 하나만 성공하며 정합성이 보장된다")
    @Test
    void 예약_생성과_슬롯_삭제가_동시에_일어날_때_정합성이_보장된다() throws InterruptedException {
        ReservationCreateCommand command = new ReservationCreateCommand("동시접근자", 예약_날짜, time3Id, themeId);

        ConcurrencyResult result = 동시_실행(
                () -> 예약_생성(command),
                () -> 슬롯_삭제(예약_날짜, time3Id, themeId)
        );

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(1);
    }

    private ConcurrencyResult 동시_실행(int threadCount, Runnable action) throws InterruptedException {
        Runnable[] actions = new Runnable[threadCount];
        for (int i = 0; i < threadCount; i++) {
            actions[i] = action;
        }
        return 동시_실행(actions);
    }

    private ConcurrencyResult 동시_실행(Runnable... actions) throws InterruptedException {
        int threadCount = actions.length;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (Runnable action : actions) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    action.run();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        startLatch.countDown();
        latch.await();
        executorService.shutdown();
        return new ConcurrencyResult(successCount.get(), failCount.get());
    }

    private long 특정_시간의_예약_개수_조회(Long timeId) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.slot().getTime().getId().equals(timeId))
                .count();
    }

    private ReservationResult 예약_생성(ReservationCreateCommand reservationCreateCommand) {
        return adminReservationService.create(reservationCreateCommand);
    }

    private void 슬롯_생성(LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation_date (date) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM reservation_date WHERE date = ?)",
                Date.valueOf(date), Date.valueOf(date)
        );
        Long dateId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_date WHERE date = ?",
                Long.class,
                Date.valueOf(date)
        );
        jdbcTemplate.update(
                "INSERT INTO reservation_slot (date_id, time_id, theme_id) SELECT ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM reservation_slot WHERE date_id = ? AND time_id = ? AND theme_id = ?)",
                dateId, timeId, themeId, dateId, timeId, themeId
        );
    }

    private void 슬롯_삭제(LocalDate date, Long timeId, Long themeId) {
        Long dateId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_date WHERE date = ?",
                Long.class,
                Date.valueOf(date)
        );
        jdbcTemplate.update(
                "DELETE FROM reservation_slot WHERE date_id = ? AND time_id = ? AND theme_id = ?",
                dateId, timeId, themeId
        );
    }

    private record ConcurrencyResult(int successCount, int failCount) {
    }
}
