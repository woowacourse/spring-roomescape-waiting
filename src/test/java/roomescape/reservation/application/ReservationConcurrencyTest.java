package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.WaitingCreateCommand;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationConcurrencyTest {

    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final String RESERVED_NAME = "리오";
    private static final String WAITING_NAME = "브라운";

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(2);
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.execute("ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    @DisplayName("예약 삭제와 대기 신청이 동시에 실행되어도 예약 없는 슬롯에 대기만 남지 않는다")
    void saveWaitingAndDeleteReservation_concurrently_success_without_orphan_waiting() throws Exception {
        // given
        Long timeId = insertTime();
        Long themeId = insertTheme();
        Long reservationId = insertReservation(RESERVED_NAME, timeId, themeId);
        WaitingCreateCommand command = new WaitingCreateCommand(WAITING_NAME, DATE, timeId, themeId);

        // when
        ConcurrentResult<Void> deleteResult = runConcurrently(
                () -> {
                    reservationService.deleteReservation(reservationId);
                    return null;
                },
                () -> {
                    waitingService.save(command);
                    return null;
                }
        );

        // then
        assertThat(deleteResult.firstSucceeded()).isTrue();
        assertThat(countWaiting(timeId, themeId)).isEqualTo(0);
        assertThat(hasReservation(timeId, themeId) || !deleteResult.secondSucceeded()).isTrue();
    }

    @Test
    @DisplayName("관리자 삭제와 사용자 취소가 동시에 실행되면 하나의 삭제만 성공한다")
    void deleteReservationAndCancelReservation_concurrently_success_with_single_delete() throws Exception {
        // given
        Long timeId = insertTime();
        Long themeId = insertTheme();
        Long reservationId = insertReservation(RESERVED_NAME, timeId, themeId);

        // when
        ConcurrentResult<Void> result = runConcurrently(
                () -> {
                    reservationService.deleteReservation(reservationId);
                    return null;
                },
                () -> {
                    reservationService.cancelReservation(reservationId, RESERVED_NAME);
                    return null;
                }
        );

        // then
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount(EntityNotFoundException.class)).isEqualTo(1);
        assertThat(countReservation(timeId, themeId)).isEqualTo(0);
    }

    @Test
    @DisplayName("대기 승격과 대기 취소가 동시에 실행되어도 취소가 성공한 대기는 예약으로 전환되지 않는다")
    void promoteWaitingAndCancelWaiting_concurrently_success_without_promoting_cancelled_waiting() throws Exception {
        // given
        Long timeId = insertTime();
        Long themeId = insertTheme();
        Long reservationId = insertReservation(RESERVED_NAME, timeId, themeId);
        Long waitingId = insertWaiting(WAITING_NAME, timeId, themeId);

        // when
        ConcurrentResult<Void> result = runConcurrently(
                () -> {
                    reservationService.deleteReservation(reservationId);
                    return null;
                },
                () -> {
                    waitingService.cancelWaiting(waitingId, WAITING_NAME);
                    return null;
                }
        );

        // then
        assertThat(result.firstSucceeded()).isTrue();
        if (result.secondSucceeded()) {
            assertThat(hasReservationByName(WAITING_NAME, timeId, themeId)).isFalse();
        }
        assertThat(countWaiting(timeId, themeId)).isEqualTo(0);
    }

    private <T> ConcurrentResult<T> runConcurrently(Callable<T> first, Callable<T> second) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<CallResult<T>> firstFuture = executorService.submit(awaitStart(first, ready, start));
        Future<CallResult<T>> secondFuture = executorService.submit(awaitStart(second, ready, start));

        ready.await();
        start.countDown();

        return new ConcurrentResult<>(firstFuture.get(), secondFuture.get());
    }

    private <T> Callable<CallResult<T>> awaitStart(
            Callable<T> callable,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            start.await();
            try {
                return CallResult.success(callable.call());
            } catch (Exception e) {
                return CallResult.failure(e);
            }
        };
    }

    private Long insertTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.now().plusHours(1));
        return jdbcTemplate.queryForObject("SELECT id FROM reservation_time", Long.class);
    }

    private Long insertTheme() {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마",
                "테마 설명",
                "https://good.com/thumb-nail/1"
        );
        return jdbcTemplate.queryForObject("SELECT id FROM theme", Long.class);
    }

    private Long insertReservation(String name, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                name,
                DATE,
                timeId,
                themeId
        );
        return jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);
    }

    private Long insertWaiting(String name, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                name,
                DATE,
                timeId,
                themeId
        );
        return jdbcTemplate.queryForObject("SELECT id FROM waiting", Long.class);
    }

    private int countReservation(Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class,
                DATE,
                timeId,
                themeId
        );
        return count == null ? 0 : count;
    }

    private int countWaiting(Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class,
                DATE,
                timeId,
                themeId
        );
        return count == null ? 0 : count;
    }

    private boolean hasReservation(Long timeId, Long themeId) {
        return countReservation(timeId, themeId) > 0;
    }

    private boolean hasReservationByName(String name, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                    SELECT COUNT(*)
                    FROM reservation
                    WHERE name = ?
                      AND date = ?
                      AND time_id = ?
                      AND theme_id = ?
                    """,
                Integer.class,
                name,
                DATE,
                timeId,
                themeId
        );
        return count != null && count > 0;
    }

    private record ConcurrentResult<T>(
            CallResult<T> first,
            CallResult<T> second
    ) {
        boolean firstSucceeded() {
            return first.succeeded();
        }

        boolean secondSucceeded() {
            return second.succeeded();
        }

        int successCount() {
            return (first.succeeded() ? 1 : 0) + (second.succeeded() ? 1 : 0);
        }

        int failureCount(Class<? extends Exception> exceptionType) {
            return (first.failedWith(exceptionType) ? 1 : 0)
                    + (second.failedWith(exceptionType) ? 1 : 0);
        }
    }

    private record CallResult<T>(
            T value,
            Exception exception
    ) {
        static <T> CallResult<T> success(T value) {
            return new CallResult<>(value, null);
        }

        static <T> CallResult<T> failure(Exception exception) {
            return new CallResult<>(null, exception);
        }

        boolean succeeded() {
            return exception == null;
        }

        boolean failedWith(Class<? extends Exception> exceptionType) {
            return exceptionType.isInstance(exception);
        }
    }
}
