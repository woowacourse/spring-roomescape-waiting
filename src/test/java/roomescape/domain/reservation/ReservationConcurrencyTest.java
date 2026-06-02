package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 같은_슬롯에_동시에_예약하면_하나만_성공한다() throws Exception {
        Long themeId = insertTheme("동시성테마");
        Long timeId = insertTime("10:00", "11:00");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<ReservationResult> firstTask = createReservationTask(
            ready, start, new ReservationRequest("유저1", LocalDate.of(2099, 12, 31), timeId, themeId)
        );
        Callable<ReservationResult> secondTask = createReservationTask(
            ready, start, new ReservationRequest("유저2", LocalDate.of(2099, 12, 31), timeId, themeId)
        );

        Future<ReservationResult> firstResult = executorService.submit(firstTask);
        Future<ReservationResult> secondResult = executorService.submit(secondTask);
        ready.await(1, TimeUnit.SECONDS);
        start.countDown();

        List<ReservationResult> results = List.of(firstResult.get(), secondResult.get());
        executorService.shutdown();

        assertThat(results).filteredOn(ReservationResult::success).hasSize(1);
        assertThat(results).extracting(ReservationResult::errorCode)
            .contains(ErrorCode.DUPLICATE_RESERVATION);
        assertReservationCount(1);
    }

    @Test
    void 같은_시간이어도_다른_슬롯이면_동시에_예약할_수_있다() throws Exception {
        Long firstThemeId = insertTheme("동시성테마1");
        Long secondThemeId = insertTheme("동시성테마2");
        Long timeId = insertTime("10:00", "11:00");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<ReservationResult> firstTask = createReservationTask(
            ready, start, new ReservationRequest("유저1", LocalDate.of(2099, 12, 31), timeId, firstThemeId)
        );
        Callable<ReservationResult> secondTask = createReservationTask(
            ready, start, new ReservationRequest("유저2", LocalDate.of(2099, 12, 31), timeId, secondThemeId)
        );

        Future<ReservationResult> firstResult = executorService.submit(firstTask);
        Future<ReservationResult> secondResult = executorService.submit(secondTask);
        ready.await(1, TimeUnit.SECONDS);
        start.countDown();

        List<ReservationResult> results = List.of(firstResult.get(), secondResult.get());
        executorService.shutdown();

        assertThat(results).allMatch(ReservationResult::success);
        assertReservationCount(2);
    }

    private Callable<ReservationResult> createReservationTask(
        CountDownLatch ready, CountDownLatch start, ReservationRequest request
    ) {
        return () -> {
            ready.countDown();
            start.await();
            try {
                reservationService.createReservation(request);
                return ReservationResult.succeeded();
            } catch (RoomescapeException exception) {
                return ReservationResult.failed(exception.getErrorCode());
            }
        };
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, image_url) VALUES (?, '설명', 'https://example.com/image.jpg')",
            name
        );
        return jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, name);
    }

    private Long insertTime(String startAt, String finishAt) {
        jdbcTemplate.update(
            "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
            startAt, finishAt
        );
        return jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt);
    }

    private void assertReservationCount(int expected) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        assertThat(count).isEqualTo(expected);
    }

    private record ReservationResult(boolean success, ErrorCode errorCode) {

        private static ReservationResult succeeded() {
            return new ReservationResult(true, null);
        }

        private static ReservationResult failed(ErrorCode errorCode) {
            return new ReservationResult(false, errorCode);
        }
    }
}
