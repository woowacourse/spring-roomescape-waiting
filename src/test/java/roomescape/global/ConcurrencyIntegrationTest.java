package roomescape.global;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.testSupport.RestAssuredTestHelper.createReservation;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.global.exception.BusinessException;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.time.service.ReservationTimeService;
import roomescape.time.service.dto.ReservationTimeCommand;
import roomescape.waiting.service.ReservationWaitingService;
import roomescape.waiting.service.dto.ReservationWaitingCommand;

@SpringWebTest
class ConcurrencyIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    private List<Integer> runConcurrentlyAndCountResults(
            Runnable runnable,
            int numberOfThread,
            Class<? extends BusinessException> expectedExceptionType
    ) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);

        CountDownLatch latch = new CountDownLatch(numberOfThread);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        AtomicInteger unexpectedErrorCount = new AtomicInteger();

        for (int i = 0; i < numberOfThread; i++) {
            executorService.submit(() -> {
                try {
                    runnable.run();
                    successCount.incrementAndGet();
                } catch (Throwable throwable) {
                    if (expectedExceptionType.isInstance(throwable)) {
                        duplicateCount.incrementAndGet();
                    } else {
                        unexpectedErrorCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        return List.of(
                successCount.get(),
                duplicateCount.get(),
                unexpectedErrorCount.get()
        );
    }

    @Test
    @DisplayName("동일한 예약 요청이 동시에 들어오면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    void saveReservation() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationService.save(new ReservationCommand(
                                "name" + java.util.UUID.randomUUID().toString(),
                                LocalDate.now().plusDays(7),
                                1L,
                                1L
                        )
                ),
                100,
                ConflictException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @Test
    @DisplayName("동일한 예약 시간을 동시에 생성하면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    void registerTime() throws InterruptedException {
        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationTimeService.save(
                        new ReservationTimeCommand(LocalTime.of(10, 0))
                ),
                100,
                ConflictException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @Test
    @DisplayName("동일한 테마를 동시에 생성하면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    void saveTheme() throws InterruptedException {
        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> themeService.save(
                        new ThemeCommand("테마", "설명", "thumbnailUrl")
                ),
                100,
                ConflictException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @Test
    @DisplayName("예약 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    void deleteReservation() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");

        createReservation("브라운", LocalDate.now().plusDays(7), 1L, 1L);

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationService.deleteById(1L, "브라운"),
                100,
                NotFoundException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @Test
    @DisplayName("테마 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    void deleteTheme() throws InterruptedException {
        //given
        createTheme("테마", "설명", "thumbnailUrl");

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> themeService.delete(1L),
                100,
                NotFoundException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @Test
    @DisplayName("예약 시간 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    void deleteTime() throws InterruptedException {
        //given
        createReservationTime("10:00");

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationTimeService.deleteById(1L),
                100,
                NotFoundException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @Test
    @DisplayName("서로 다른 본인 예약을 같은 슬롯으로 동시에 수정하면 하나만 성공하고 하나는 중복 예외가 발생한다")
    void updateMyReservation() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createReservationTime("11:00");
        createReservationTime("12:00");
        createTheme("테마", "설명", "thumbnailUrl");

        Long reservationId1 = createReservation("브라운", LocalDate.now().plusDays(7), 1L, 1L);
        Long reservationId2 = createReservation("코니", LocalDate.now().plusDays(7), 2L, 1L);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        AtomicInteger unexpectedErrorCount = new AtomicInteger();

        //when
        List<Runnable> tasks = List.of(
                () -> reservationService.update(
                        new ReservationUpdateCommand(LocalDate.now().plusDays(14), 3L),
                        reservationId1,
                        "브라운"
                ),
                () -> reservationService.update(
                        new ReservationUpdateCommand(LocalDate.now().plusDays(14), 3L),
                        reservationId2,
                        "코니"
                )
        );

        for (Runnable task : tasks) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    task.run();
                    successCount.incrementAndGet();
                } catch (ConflictException e) {
                    duplicateCount.incrementAndGet();
                } catch (Throwable throwable) {
                    unexpectedErrorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        //then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(1);
        assertThat(unexpectedErrorCount.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("동일한 예약 대기 신청이 동시에 들어오면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    void saveReservationWaiting() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        createReservation("브라운", LocalDate.now().plusDays(7), 1L, 1L);

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationWaitingService.save(new ReservationWaitingCommand(
                                "name",
                                LocalDate.now().plusDays(7),
                                1L,
                                1L
                        )
                ),
                100,
                BusinessException.class
        );

        //then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @Test
    @DisplayName("동일한 예약 대기 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    void delete() throws InterruptedException {
        // given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        createReservation("브라운", LocalDate.now().plusDays(7), 1L, 1L);
        reservationWaitingService.save(new ReservationWaitingCommand(
                "포비",
                LocalDate.now().plusDays(7),
                1L,
                1L
        ));

        // when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationWaitingService.deleteById(1L, "포비"),
                100,
                NotFoundException.class
        );

        // then
        assertThat(result.get(0)).isEqualTo(1);
        assertThat(result.get(1)).isEqualTo(99);
        assertThat(result.get(2)).isEqualTo(0);
    }

    @Test
    @DisplayName("동일한 사용자가 동일한 시간에 서로 다른 테마를 동시에 예약하면 하나만 성공하고 하나는 예외가 발생한다")
    void saveSameTimeDifferentThemeReservation() throws InterruptedException {
        // given
        createReservationTime("10:00");
        createTheme("테마1", "설명1", "url1");
        createTheme("테마2", "설명2", "url2");

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        AtomicInteger unexpectedErrorCount = new AtomicInteger();

        List<Runnable> tasks = List.of(
                () -> reservationService.save(new ReservationCommand(
                        "브라운",
                        LocalDate.now().plusDays(7),
                        1L,
                        1L
                )),
                () -> reservationService.save(new ReservationCommand(
                        "브라운",
                        LocalDate.now().plusDays(7),
                        1L,
                        2L
                ))
        );

        for (Runnable task : tasks) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    task.run();
                    successCount.incrementAndGet();
                } catch (InvalidBusinessStateException e) {
                    duplicateCount.incrementAndGet();
                } catch (Throwable throwable) {
                    unexpectedErrorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(1);
        assertThat(unexpectedErrorCount.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("동일한 사용자가 동일한 시간에 예약과 예약 대기를 동시에 요청하면 하나만 성공하고 하나는 예외가 발생한다")
    void saveSameTimeReservationAndWaiting() throws InterruptedException {
        // given
        createReservationTime("10:00");
        createTheme("테마1", "설명1", "url1");
        createTheme("테마2", "설명2", "url2");
        createReservation("다른사람", LocalDate.now().plusDays(7), 1L, 1L);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        AtomicInteger unexpectedErrorCount = new AtomicInteger();

        List<Runnable> tasks = List.of(
                () -> reservationService.save(new ReservationCommand(
                        "브라운",
                        LocalDate.now().plusDays(7),
                        1L,
                        2L
                )),
                () -> reservationWaitingService.save(new ReservationWaitingCommand(
                        "브라운",
                        LocalDate.now().plusDays(7),
                        1L,
                        1L
                ))
        );

        for (Runnable task : tasks) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    task.run();
                    successCount.incrementAndGet();
                } catch (InvalidBusinessStateException e) {
                    duplicateCount.incrementAndGet();
                } catch (Throwable throwable) {
                    unexpectedErrorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(1);
        assertThat(unexpectedErrorCount.get()).isEqualTo(0);
    }
}
