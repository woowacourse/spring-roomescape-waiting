package roomescape.reservation.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.application.dto.BookingCreateCommand;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.application.service.WaitingService;
import roomescape.support.ApiIntegrationTestHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReservationConcurrencyTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ReservationService reservationService;

    private ApiIntegrationTestHelper testHelper;

    private static final LocalDate BASE_DATE = LocalDate.of(2028, 5, 6);
    private static final LocalDateTime PAST = LocalDateTime.of(2000, 1, 1, 0, 0);

    @BeforeEach
    void setUp() {
        testHelper = new ApiIntegrationTestHelper(jdbcTemplate);
        testHelper.clearDatabase();
    }

    @DisplayName("동일 슬롯에 동시 예약 신청 시 1명만 예약되고 나머지는 대기에 쌓인다.")
    @Test
    void only_one_reserved_and_rest_queued_when_concurrent_requests() throws InterruptedException {
        Long themeId = testHelper.insertTheme("테마", "설명", "img.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < threadCount; i++) {
            String name = "사용자" + i;
            submit(executor, latch, () -> waitingService.save(new BookingCreateCommand(name, BASE_DATE, themeId, timeId), PAST));
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        int reservationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        int waitingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM waiting", Integer.class);

        assertThat(reservationCount).isEqualTo(1);
        assertThat(waitingCount).isEqualTo(threadCount - 1);
    }

    @DisplayName("예약 취소와 예약 신청이 동시에 발생해도 고아 대기가 남지 않는다.")
    @Test
    void no_orphan_waiting_when_cancel_and_save_concurrent() throws InterruptedException {
        Long themeId = testHelper.insertTheme("테마", "설명", "img.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        int trials = 1000;
        List<Long> reservationIds = new ArrayList<>();
        for (int i = 0; i < trials; i++) {
            LocalDate date = BASE_DATE.plusDays(i);
            reservationIds.add(testHelper.insertReservation("타스", date, themeId, timeId));
        }

        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < trials; i++) {
            LocalDate date = BASE_DATE.plusDays(i);
            Long reservationId = reservationIds.get(i);

            submit(executor, latch, () -> reservationService.delete(reservationId, "타스", PAST));
            submit(executor, latch, () -> waitingService.save(new BookingCreateCommand("카야", date, themeId, timeId), PAST));
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        int waitingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM waiting", Integer.class);
        assertThat(waitingCount).isZero();
    }

    private void submit(ExecutorService executor, CountDownLatch latch, Runnable task) {
        executor.submit(() -> {
            try {
                latch.await();
                task.run();
            } catch (Exception ignored) {
            }
        });
    }
}
