package roomescape.reservation.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.global.exception.ConflictException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.IntFunction;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReservationConcurrencyTest {

    private static final int REQUEST_COUNT = 10;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM reservation_history");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    @DisplayName("다른 사용자들이 같은 슬롯에 동시에 예약하면 하나만 예약 확정되고 나머지는 대기가 된다.")
    void create_success_whenDifferentUsersReserveSameSlotConcurrently() throws Exception {
        ReservationTime time = saveReservationTime(10, 1);
        Theme theme = saveTheme("동시 예약 테스트");
        LocalDate date = LocalDate.now().plusDays(1);

        List<ExecutionResult> results = runConcurrently(REQUEST_COUNT, index ->
                reservationService.create("사용자-" + index, date, time.getId(), theme.getId())
        );

        assertThat(results).allMatch(ExecutionResult::isSuccess);
        assertQueueIsConsistent(date, theme, REQUEST_COUNT);
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 동시에 예약하면 하나만 성공하고 나머지는 중복으로 거부된다.")
    void create_fail_whenSameUserReservesSameSlotConcurrently() throws Exception {
        ReservationTime time = saveReservationTime(10, 2);
        Theme theme = saveTheme("동시 중복 예약 테스트");
        LocalDate date = LocalDate.now().plusDays(2);

        List<ExecutionResult> results = runConcurrently(REQUEST_COUNT, index ->
                reservationService.create("브라운", date, time.getId(), theme.getId())
        );

        assertThat(results).filteredOn(ExecutionResult::isSuccess).hasSize(1);
        assertThat(results)
                .filteredOn(result -> result.throwable() instanceof ConflictException)
                .hasSize(REQUEST_COUNT - 1);
        assertQueueIsConsistent(date, theme, 1);
    }

    @Test
    @DisplayName("다른 사용자들이 동시에 같은 슬롯으로 예약을 변경하면 하나만 예약 확정되고 나머지는 대기가 된다.")
    void updateDateTime_success_whenDifferentUsersMoveToSameSlotConcurrently() throws Exception {
        ReservationTime originalTime = saveReservationTime(10, 3);
        ReservationTime targetTime = saveReservationTime(10, 4);
        Theme theme = saveTheme("동시 예약 변경 테스트");
        LocalDate originalDate = LocalDate.now().plusDays(3);
        LocalDate targetDate = LocalDate.now().plusDays(4);
        List<Reservation> reservations = new ArrayList<>();
        for (int index = 0; index < REQUEST_COUNT; index++) {
            reservations.add(reservationService.create(
                    "변경 사용자-" + index,
                    originalDate,
                    originalTime.getId(),
                    theme.getId()
            ));
        }

        List<ExecutionResult> results = runConcurrently(REQUEST_COUNT, index -> {
            Reservation reservation = reservations.get(index);
            return reservationService.updateDateTime(
                    reservation.getId(),
                    reservation.getName(),
                    targetDate,
                    targetTime.getId()
            );
        });

        assertThat(results).allMatch(ExecutionResult::isSuccess);
        assertThat(results)
                .extracting(ExecutionResult::reservation)
                .allSatisfy(reservation -> {
                    assertThat(reservation.getDate()).isEqualTo(targetDate);
                    assertThat(reservation.getTime()).isEqualTo(targetTime);
                });
        assertQueueIsConsistent(targetDate, theme, REQUEST_COUNT);
    }

    private ReservationTime saveReservationTime(int hour, int minute) {
        return reservationTimeRepository.save(new ReservationTime(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name) {
        return themeRepository.save(new Theme(
                name,
                name + "용 테마",
                "https://example.com/theme.png"
        ));
    }

    private void assertQueueIsConsistent(LocalDate date, Theme theme, int reservationCount) {
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, theme.getId());

        assertThat(reservations).hasSize(reservationCount);
        assertThat(reservations)
                .extracting(Reservation::getStatus)
                .filteredOn(status -> status == ReservationStatus.RESERVED)
                .hasSize(1);
        assertThat(reservations)
                .extracting(Reservation::getStatus)
                .filteredOn(status -> status == ReservationStatus.WAITING)
                .hasSize(reservationCount - 1);
        assertThat(reservations)
                .extracting(Reservation::getWaitingRank)
                .containsExactlyInAnyOrderElementsOf(LongStream.range(0, reservationCount).boxed().toList());
    }

    private List<ExecutionResult> runConcurrently(
            int taskCount,
            IntFunction<Reservation> task
    ) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(taskCount);
        CountDownLatch readyLatch = new CountDownLatch(taskCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            List<Future<ExecutionResult>> futures = new ArrayList<>();
            for (int index = 0; index < taskCount; index++) {
                final int taskIndex = index;
                futures.add(executorService.submit(() -> {
                    readyLatch.countDown();
                    startLatch.await();
                    try {
                        return ExecutionResult.success(task.apply(taskIndex));
                    } catch (Throwable throwable) {
                        return ExecutionResult.failure(throwable);
                    }
                }));
            }

            readyLatch.await();
            startLatch.countDown();

            List<ExecutionResult> results = new ArrayList<>();
            for (Future<ExecutionResult> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            executorService.shutdownNow();
        }
    }

    private record ExecutionResult(Reservation reservation, Throwable throwable) {
        private static ExecutionResult success(Reservation reservation) {
            return new ExecutionResult(reservation, null);
        }

        private static ExecutionResult failure(Throwable throwable) {
            return new ExecutionResult(null, throwable);
        }

        private boolean isSuccess() {
            return throwable == null;
        }
    }
}
