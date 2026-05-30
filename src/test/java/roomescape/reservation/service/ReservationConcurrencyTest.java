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
import java.util.concurrent.Callable;
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
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 1)));
        Theme theme = themeRepository.save(new Theme("동시 예약 테스트", "동시 예약 테스트용 테마", "https://example.com/concurrent.png"));
        LocalDate date = LocalDate.now().plusDays(1);

        List<CreateResult> results = runConcurrently(REQUEST_COUNT, index ->
                create("사용자-" + index, date, time, theme)
        );

        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, theme.getId());

        assertThat(results).allMatch(CreateResult::isSuccess);
        assertThat(reservations).hasSize(REQUEST_COUNT);
        assertThat(reservations)
                .extracting(Reservation::getStatus)
                .filteredOn(status -> status == ReservationStatus.RESERVED)
                .hasSize(1);
        assertThat(reservations)
                .extracting(Reservation::getStatus)
                .filteredOn(status -> status == ReservationStatus.WAITING)
                .hasSize(REQUEST_COUNT - 1);
        assertThat(reservations)
                .extracting(Reservation::getWaitingRank)
                .containsExactlyInAnyOrderElementsOf(LongStream.range(0, REQUEST_COUNT).boxed().toList());
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 동시에 예약하면 하나만 성공하고 나머지는 중복으로 거부된다.")
    void create_fail_whenSameUserReservesSameSlotConcurrently() throws Exception {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 2)));
        Theme theme = themeRepository.save(new Theme("동시 중복 예약 테스트", "동시 중복 예약 테스트용 테마", "https://example.com/duplicate.png"));
        LocalDate date = LocalDate.now().plusDays(2);

        List<CreateResult> results = runConcurrently(REQUEST_COUNT, index ->
                create("브라운", date, time, theme)
        );

        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, theme.getId());

        assertThat(results).filteredOn(CreateResult::isSuccess).hasSize(1);
        assertThat(results)
                .filteredOn(result -> result.throwable() instanceof ConflictException)
                .hasSize(REQUEST_COUNT - 1);
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservations.get(0).getWaitingRank()).isZero();
    }

    private CreateResult create(String name, LocalDate date, ReservationTime time, Theme theme) {
        try {
            reservationService.create(name, date, time.getId(), theme.getId());
            return CreateResult.success();
        } catch (Throwable throwable) {
            return CreateResult.failure(throwable);
        }
    }

    private List<CreateResult> runConcurrently(
            int taskCount,
            IntFunction<CreateResult> task
    ) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(taskCount);
        CountDownLatch readyLatch = new CountDownLatch(taskCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            List<Future<CreateResult>> futures = new ArrayList<>();
            for (int index = 0; index < taskCount; index++) {
                final int taskIndex = index;
                futures.add(executorService.submit(callable(readyLatch, startLatch, () -> task.apply(taskIndex))));
            }

            readyLatch.await();
            startLatch.countDown();

            List<CreateResult> results = new ArrayList<>();
            for (Future<CreateResult> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            executorService.shutdownNow();
        }
    }

    private Callable<CreateResult> callable(
            CountDownLatch readyLatch,
            CountDownLatch startLatch,
            Callable<CreateResult> task
    ) {
        return () -> {
            readyLatch.countDown();
            startLatch.await();
            return task.call();
        };
    }

    private record CreateResult(Throwable throwable) {
        private static CreateResult success() {
            return new CreateResult(null);
        }

        private static CreateResult failure(Throwable throwable) {
            return new CreateResult(throwable);
        }

        private boolean isSuccess() {
            return throwable == null;
        }
    }
}
