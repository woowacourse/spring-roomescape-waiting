package roomescape.reservation.service;

import java.util.ArrayDeque;
import java.util.Queue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InfrastructureException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(
        statements = {
                "DELETE FROM reservation_history",
                "DELETE FROM reservation",
                "DELETE FROM reservation_time",
                "DELETE FROM theme",
                "ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE theme ALTER COLUMN id RESTART WITH 1",
                "ALTER SEQUENCE reservation_request_order_seq RESTART WITH 1"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class ReservationServiceConcurrencyTest {

    private static final String NAME = "브라운";
    private static final String OTHER_NAME = "레아";
    private static final int CONCURRENT_REQUEST_COUNT = 100;
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate NEXT_FUTURE_DATE = LocalDate.now().plusDays(2);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("여러 사용자가 같은 슬롯을 동시에 예약하면 한 명만 예약 확정되고 나머지는 대기가 된다.")
    void create_concurrently_sameSlot_reservesOneAndWaitsOthers() throws InterruptedException {
        // given
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();

        // when
        List<Throwable> failures = runConcurrently(index -> {
            String name = "예약자-" + index;
            reservationService.create(name, FUTURE_DATE, time.getId(), theme.getId());
        });

        // then
        List<Reservation> reservations = reservationService.findAll();

        assertThat(failures).isEmpty();
        assertThat(reservations).hasSize(CONCURRENT_REQUEST_COUNT);
        assertThat(reservations)
                .filteredOn(reservation -> reservation.getStatus() == ReservationStatus.RESERVED)
                .singleElement()
                .satisfies(reservation -> assertThat(reservation.getWaitingRank()).isZero());
        assertThat(reservations)
                .filteredOn(reservation -> reservation.getStatus() == ReservationStatus.WAITING)
                .hasSize(CONCURRENT_REQUEST_COUNT - 1);
        assertThat(reservations)
                .extracting(Reservation::getWaitingRank)
                .containsExactlyInAnyOrderElementsOf(
                        LongStream.range(0, CONCURRENT_REQUEST_COUNT)
                                .boxed()
                                .toList()
                );
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯을 동시에 예약하면 하나만 생성된다.")
    void create_concurrently_sameUserAndSameSlot_createsOnlyOne() throws InterruptedException {
        // given
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();

        // when
        List<Throwable> failures = runConcurrently(
                index -> reservationService.create(NAME, FUTURE_DATE, time.getId(), theme.getId())
        );

        // then
        assertThat(reservationService.findAll())
                .singleElement()
                .satisfies(reservation -> {
                    assertThat(reservation.getName()).isEqualTo(NAME);
                    assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
                    assertThat(reservation.getWaitingRank()).isZero();
                });
        assertThat(failures)
                .hasSize(CONCURRENT_REQUEST_COUNT - 1)
                .allSatisfy(throwable ->
                        assertThat(throwable).isInstanceOfAny(
                                ConflictException.class,
                                DataIntegrityViolationException.class
                        )
                );
    }

    @Test
    @DisplayName("같은 예약을 동시에 취소해도 취소 이력은 하나만 남고 다음 대기가 예약 확정된다.")
    void cancel_concurrently_sameReservation_recordsHistoryOnceAndPromotesWaiting() throws InterruptedException {
        // given
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        Reservation reservedReservation = reservationService.create(NAME, FUTURE_DATE, time.getId(), theme.getId());
        Reservation waitingReservation = reservationService.create(OTHER_NAME, FUTURE_DATE, time.getId(), theme.getId());

        // when
        List<Throwable> failures = runConcurrently(
                index -> reservationService.cancel(reservedReservation.getId(), NAME)
        );

        // then
        Reservation promotedReservation = reservationService.findByName(OTHER_NAME).get(0);

        assertThat(failures)
                .allSatisfy(throwable -> assertThat(throwable).isInstanceOf(InfrastructureException.class));
        assertThat(countHistoryByReservationId(reservedReservation.getId())).isEqualTo(1);
        assertThat(promotedReservation.getId()).isEqualTo(waitingReservation.getId());
        assertThat(promotedReservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(promotedReservation.getWaitingRank()).isZero();
    }

    @Test
    @DisplayName("같은 예약을 동시에 변경해도 기존 예약 이력과 새 예약은 하나씩만 남는다.")
    void updateDateTime_concurrently_sameReservation_recordsHistoryOnceAndCreatesOnlyOneNewReservation()
            throws InterruptedException {
        // given
        ReservationTime time = saveReservationTime(10);
        ReservationTime newTime = saveReservationTime(11);
        Theme theme = saveTheme();
        Reservation reservation = reservationService.create(NAME, FUTURE_DATE, time.getId(), theme.getId());

        // when
        List<Throwable> failures = runConcurrently(
                index -> reservationService.updateDateTime(reservation.getId(), NAME, NEXT_FUTURE_DATE, newTime.getId())
        );

        // then
        assertThat(failures)
                .allSatisfy(throwable ->
                        assertThat(throwable).isInstanceOfAny(
                                ConflictException.class,
                                InfrastructureException.class,
                                NotFoundException.class
                        )
                );
        assertThat(countHistoryByReservationId(reservation.getId())).isEqualTo(1);
        assertThat(reservationService.findAll())
                .singleElement()
                .satisfies(updatedReservation -> {
                    assertThat(updatedReservation.getId()).isNotEqualTo(reservation.getId());
                    assertThat(updatedReservation.getName()).isEqualTo(NAME);
                    assertThat(updatedReservation.getDate()).isEqualTo(NEXT_FUTURE_DATE);
                    assertThat(updatedReservation.getTime()).isEqualTo(newTime);
                    assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
                    assertThat(updatedReservation.getWaitingRank()).isZero();
                });
    }

    private List<Throwable> runConcurrently(ConcurrentTask task) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_REQUEST_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(CONCURRENT_REQUEST_COUNT);

        try {
            List<Callable<Throwable>> tasks = IntStream.range(0, CONCURRENT_REQUEST_COUNT)
                    .mapToObj(index -> (Callable<Throwable>) () -> {
                        try {
                            barrier.await(10, TimeUnit.SECONDS);
                            task.run(index);
                            return null;
                        } catch (Throwable throwable) {
                            return throwable;
                        }
                    })
                    .toList();

            List<Future<Throwable>> futures = executorService.invokeAll(tasks, 60, TimeUnit.SECONDS);
            List<Throwable> failures = new ArrayList<>();

            for (Future<Throwable> future : futures) {
                if (future.isCancelled()) {
                    failures.add(new AssertionError("동시성 작업이 제한 시간 안에 끝나지 않았습니다."));
                    continue;
                }

                failures.addAll(futureFailure(future));
            }

            return failures;
        } finally {
            executorService.shutdownNow();
        }
    }

    private List<Throwable> futureFailure(Future<Throwable> future) {
        try {
            Throwable throwable = future.get();
            if (throwable == null) {
                return List.of();
            }

            return List.of(throwable);
        } catch (Exception exception) {
            return List.of(exception);
        }
    }

    private Integer countHistoryByReservationId(Long reservationId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_history WHERE reservation_id = ?",
                Integer.class,
                reservationId
        );
    }

    private ReservationTime saveReservationTime(int hour) {
        return reservationTimeRepository.save(ReservationTime.create(LocalTime.of(hour, 0)));
    }

    private Theme saveTheme() {
        return themeRepository.save(Theme.create(
                "동시성 탈출",
                "동시에 예약해도 대기열 순서가 유지되는지 확인하는 테마입니다.",
                "https://example.com/concurrency-theme.png"
        ));
    }

    @FunctionalInterface
    private interface ConcurrentTask {
        void run(int index) throws Exception;
    }
}
