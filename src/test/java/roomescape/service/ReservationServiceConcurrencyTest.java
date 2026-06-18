package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.exception.DomainErrorCode.DUPLICATE_RESERVATION;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationServiceConcurrencyTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 같은_슬롯에_동시에_예약을_신청하면_하나는_예약되고_나머지는_대기로_등록된다() throws Exception {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        List<String> names = List.of("브라운", "브리", "워니");

        int requestCount = names.size();
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<ReservationWithStatus>> futures = names.stream()
                    .map(name -> executorService.submit(createApplyReservationTask(name, reservationTime, theme, ready,
                            start)))
                    .toList();

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<ReservationWithStatus> results = new ArrayList<>();
            for (Future<ReservationWithStatus> future : futures) {
                results.add(future.get(5, TimeUnit.SECONDS));
            }

            assertThat(results)
                    .extracting(ReservationWithStatus::getStatus)
                    .containsExactlyInAnyOrder(
                            ReservationStatus.RESERVED,
                            ReservationStatus.WAITING,
                            ReservationStatus.WAITING
                    );

            List<ReservationWithStatus> savedResults = names.stream()
                    .flatMap(name -> reservationService.getMyReservations(name).stream())
                    .toList();

            assertThat(savedResults)
                    .hasSize(3)
                    .extracting(ReservationWithStatus::getStatus)
                    .containsExactlyInAnyOrder(
                            ReservationStatus.RESERVED,
                            ReservationStatus.WAITING,
                            ReservationStatus.WAITING
                    );
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void 같은_슬롯에_동시에_예약을_변경하면_하나는_예약이_변경되고_나머지는_예외를_반환한다() throws Exception {
        List<String> names = List.of("브라운", "브리", "워니");
        List<ReservationTime> originalTimes = List.of(
                createReservationTime(LocalTime.of(10, 0)),
                createReservationTime(LocalTime.of(11, 0)),
                createReservationTime(LocalTime.of(13, 0))
        );
        Theme theme = createTheme();
        List<ReservationWithStatus> reservations = IntStream.range(0, names.size())
                .mapToObj(i -> reservationService.applyReservation(
                        createReservationRequest(names.get(i), originalTimes.get(i), theme)))
                .toList();

        ReservationTime updateTime = createReservationTime(LocalTime.of(12, 0));
        LocalDate updateDate = FUTURE_SECOND_DATE.plusDays(1);

        int requestCount = names.size();
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Reservation>> futures = reservations.stream()
                    .map(reservation -> executorService.submit(
                            createUpdateMyReservationTask(
                                    reservation.getId(),
                                    reservation.getName(),
                                    updateDate,
                                    updateTime.getId(),
                                    ready, start
                            )
                    ))
                    .toList();

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<Reservation> successes = new ArrayList<>();
            List<Throwable> failures = new ArrayList<>();
            for (Future<Reservation> future : futures) {
                try {
                    successes.add(future.get(5, TimeUnit.SECONDS));
                } catch (ExecutionException e) {
                    failures.add(e.getCause());
                }
            }

            assertThat(successes).hasSize(1);
            assertThat(successes.getFirst())
                    .extracting(
                            Reservation::getDate,
                            r -> r.getTime().getId(),
                            r -> r.getTime().getStartAt()
                    ).containsExactly(updateDate, updateTime.getId(), updateTime.getStartAt());
            assertThat(failures)
                    .hasSize(requestCount - 1)
                    .allSatisfy(failure ->
                            assertThat(failure)
                                    .isInstanceOfSatisfying(RoomEscapeException.class,
                                            e -> assertThat(e.code()).isEqualTo(DUPLICATE_RESERVATION))
                    );

        } finally {
            executorService.shutdownNow();
        }
    }

    private Callable<ReservationWithStatus> createApplyReservationTask(
            String name,
            ReservationTime reservationTime,
            Theme theme,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            start.await();
            return reservationService.applyReservation(createReservationRequest(name, reservationTime, theme));
        };
    }

    private Callable<Reservation> createUpdateMyReservationTask(
            Long id,
            String name,
            LocalDate updateDate,
            Long timeId,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            start.await();
            return reservationService.updateMyReservationAndPromoteWaitlist(id, name,
                    new ReservationUpdateRequest(updateDate, timeId));
        };
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        return timeRepository.save(reservationTime);
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        return themeRepository.save(theme);
    }

    private ReservationRequest createReservationRequest(String name, ReservationTime time, Theme theme) {
        return new ReservationRequest(
                name,
                FUTURE_SECOND_DATE,
                time.getId(),
                theme.getId()
        );
    }
}
