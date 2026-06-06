package roomescape.reservationWaiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.exception.DuplicateReservationWaitingException;
import roomescape.reservationWaiting.exception.ReservationWaitingNotFoundException;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.support.ConcurrentExecutor;
import roomescape.support.ConcurrentResult;
import roomescape.support.ServiceIntegrationTest;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.time.service.ReservationTimeService;
import roomescape.time.service.dto.ReservationTimeCommand;

public class ReservationWaitingServiceIntegrationTest extends ServiceIntegrationTest {

    private static final long RESERVATION_ID = 1L;
    private static final long WAITING_ID = 1L;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Autowired
    ReservationWaitingService reservationWaitingService;

    @Autowired
    ReservationService reservationService;

    @MockitoSpyBean
    ReservationWaitingRepository reservationWaitingRepository;

    @DisplayName("동일한 예약 대기 신청이 동시에 들어오면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void makeReservationWaitingTest_duplicate() throws InterruptedException {
        //given
        reservationTimeService.registerReservationTime(
                new ReservationTimeCommand(LocalTime.of(10, 0))
        );
        themeService.registerTheme(
                new ThemeCommand(
                        "테마", "설명", "url"
                )
        );

        reservationService.makeReservation(
                new ReservationCommand(
                        "brown", LocalDate.of(2026, 5, 5), 1L, 1L
                )
        );

        //when
        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(100, () -> {
            try {
                reservationWaitingService.makeReservationWaiting(new ReservationWaitingCommand(
                        "pobi",
                        LocalDate.of(2026, 5, 5),
                        1L,
                        1L
                ));

                return ConcurrentResult.withSuccess();
            } catch (Throwable e) {
                return ConcurrentResult.withFail(e);
            }
        });

        //then
        assertThat(results).filteredOn(ConcurrentResult::success).hasSize(1);

        assertThat(results).filteredOn(result -> !result.success()).hasSize(99);
        assertThat(results)
                .filteredOn(result -> !result.success())
                .extracting(ConcurrentResult::exception)
                .allMatch(DuplicateReservationWaitingException.class::isInstance);
    }

    @DisplayName("예약 대기 생성 중에는 동일 슬롯의 예약을 삭제/변경할 수 없다.")
    @Test
    void makeReservationWaitingTest_update_lock() throws Exception {
        //given
        reservationTimeService.registerReservationTime(
                new ReservationTimeCommand(LocalTime.of(10, 0))
        );
        reservationTimeService.registerReservationTime(
                new ReservationTimeCommand(LocalTime.of(11, 0))
        );
        themeService.registerTheme(
                new ThemeCommand(
                        "테마", "설명", "url"
                )
        );

        reservationService.makeReservation(
                new ReservationCommand(
                        "brown", LocalDate.of(2026, 5, 5), 1L, 1L
                )
        );

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch waitingSaveEntered = new CountDownLatch(1);
        CountDownLatch allowWaitingSave = new CountDownLatch(1);

        doAnswer(invocation -> {
            waitingSaveEntered.countDown();
            assertThat(allowWaitingSave.await(2, TimeUnit.SECONDS)).isTrue();
            return invocation.callRealMethod();
        }).when(reservationWaitingRepository).save(any(ReservationWaiting.class));

        try {
            Future<ReservationWaiting> waitingFuture = executorService.submit(() ->
                    reservationWaitingService.makeReservationWaiting(
                            new ReservationWaitingCommand(
                                    "pobi",
                                    LocalDate.of(2026, 5, 5),
                                    1L,
                                    1L
                            )
                    )
            );

            assertThat(waitingSaveEntered.await(2, TimeUnit.SECONDS)).isTrue();

            Future<?> updateFuture = executorService.submit(() ->
                    reservationService.updateReservation(
                            new ReservationUpdateCommand(LocalDate.of(2026, 5, 5), 2L),
                            RESERVATION_ID,
                            "brown"
                    )
            );

            Thread.sleep(200);
            assertThat(updateFuture.isDone()).isFalse();

            //when
            allowWaitingSave.countDown();
            waitingFuture.get(2, TimeUnit.SECONDS);
            updateFuture.get(2, TimeUnit.SECONDS);

            //then
            assertAll(
                    () -> assertReservationName(LocalDate.of(2026, 5, 5), 1L, 1L, "pobi"),
                    () -> assertReservationName(LocalDate.of(2026, 5, 5), 2L, 1L, "brown"),
                    () -> assertWaitingNotExists(WAITING_ID)
            );
        } finally {
            executorService.shutdownNow();
        }
    }

    @DisplayName("예약 대기 생성 중에는 동일 슬롯의 예약을 삭제할 수 없다.")
    @Test
    void makeReservationWaitingTest_delete_lock() throws Exception {
        //given
        reservationTimeService.registerReservationTime(
                new ReservationTimeCommand(LocalTime.of(10, 0))
        );
        themeService.registerTheme(
                new ThemeCommand(
                        "테마", "설명", "url"
                )
        );

        reservationService.makeReservation(
                new ReservationCommand(
                        "brown", LocalDate.of(2026, 5, 5), 1L, 1L
                )
        );

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch waitingSaveEntered = new CountDownLatch(1);
        CountDownLatch allowWaitingSave = new CountDownLatch(1);

        doAnswer(invocation -> {
            waitingSaveEntered.countDown();
            assertThat(allowWaitingSave.await(2, TimeUnit.SECONDS)).isTrue();
            return invocation.callRealMethod();
        }).when(reservationWaitingRepository).save(any(ReservationWaiting.class));

        try {
            Future<ReservationWaiting> waitingFuture = executorService.submit(() ->
                    reservationWaitingService.makeReservationWaiting(
                            new ReservationWaitingCommand(
                                    "pobi",
                                    LocalDate.of(2026, 5, 5),
                                    1L,
                                    1L
                            )
                    )
            );

            assertThat(waitingSaveEntered.await(2, TimeUnit.SECONDS)).isTrue();

            Future<?> deleteFuture = executorService.submit(() ->
                    reservationService.deleteReservationById(RESERVATION_ID)
            );

            Thread.sleep(200);
            assertThat(deleteFuture.isDone()).isFalse();

            //when
            allowWaitingSave.countDown();
            waitingFuture.get(2, TimeUnit.SECONDS);
            deleteFuture.get(2, TimeUnit.SECONDS);

            //then
            assertAll(
                    () -> assertReservationName(LocalDate.of(2026, 5, 5), 1L, 1L, "pobi"),
                    () -> assertWaitingNotExists(WAITING_ID)
            );
        } finally {
            executorService.shutdownNow();
        }
    }

    @DisplayName("예약 대기 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void deleteReservationWaitingTest_duplicate() throws InterruptedException {
        //given
        reservationTimeService.registerReservationTime(
                new ReservationTimeCommand(LocalTime.of(10, 0))
        );
        themeService.registerTheme(
                new ThemeCommand(
                        "테마", "설명", "url"
                )
        );

        reservationService.makeReservation(
                new ReservationCommand(
                        "brown", LocalDate.of(2026, 5, 5), 1L, 1L
                )
        );
        reservationWaitingService.makeReservationWaiting(
                new ReservationWaitingCommand(
                        "pobi", LocalDate.of(2026, 5, 5), 1L, 1L
                )
        );

        //when
        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(100, () -> {
            try {
                reservationWaitingService.deleteReservationWaitingById(WAITING_ID, "pobi");

                return ConcurrentResult.withSuccess();
            } catch (Throwable e) {
                return ConcurrentResult.withFail(e);
            }
        });

        //then
        assertThat(results).filteredOn(ConcurrentResult::success).hasSize(1);

        assertThat(results).filteredOn(result -> !result.success()).hasSize(99);
        assertThat(results)
                .filteredOn(result -> !result.success())
                .extracting(ConcurrentResult::exception)
                .allMatch(ReservationWaitingNotFoundException.class::isInstance);
    }
}
