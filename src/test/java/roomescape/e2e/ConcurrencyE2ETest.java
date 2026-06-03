package roomescape.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.global.exception.BusinessException;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservationWaiting.exception.DuplicateReservationWaitingException;
import roomescape.reservationWaiting.exception.ReservationWaitingNotFoundException;
import roomescape.reservationWaiting.service.ReservationWaitingService;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.theme.exception.DuplicateThemeException;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.time.exception.DuplicateTimeException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.service.ReservationTimeService;
import roomescape.time.service.dto.ReservationTimeCommand;

class ConcurrencyE2ETest extends E2ETest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationWaitingService reservationWaitingService;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;


    @DisplayName("동일한 예약 요청이 동시에 들어오면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void makeReservation() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationService.makeReservation(new ReservationCommand(
                                "name",
                                LocalDate.of(2026, 5, 15),
                                1L,
                                1L
                        )
                ),
                100,
                DuplicateReservationException.class
        );

        //then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1),
                () -> assertThat(result.get(1)).isEqualTo(99),
                () -> assertThat(result.get(2)).isEqualTo(0)
        );
    }

    private void createReservationTime(String startAt) {
        Map<String, Object> reservationTime = new HashMap<>();
        reservationTime.put("startAt", startAt);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/admin/times")
                .then().statusCode(201);
    }

    private void createTheme(String name, String description, String thumbnailUrl) {
        Map<String, Object> theme = new HashMap<>();
        theme.put("name", name);
        theme.put("description", description);
        theme.put("thumbnailUrl", thumbnailUrl);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().statusCode(201);
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

    @DisplayName("동일한 예약 시간을 동시에 생성하면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void registerTime() throws InterruptedException {
        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationTimeService.registerReservationTime(
                        new ReservationTimeCommand(LocalTime.of(10, 0))
                ),
                100,
                DuplicateTimeException.class
        );

        //then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1),
                () -> assertThat(result.get(1)).isEqualTo(99),
                () -> assertThat(result.get(2)).isEqualTo(0)
        );
    }

    @DisplayName("동일한 테마를 동시에 생성하면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void registerTheme() throws InterruptedException {
        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> themeService.registerTheme(
                        new ThemeCommand("테마", "설명", "thumbnailUrl")
                ),
                100,
                DuplicateThemeException.class
        );

        //then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1),
                () -> assertThat(result.get(1)).isEqualTo(99),
                () -> assertThat(result.get(2)).isEqualTo(0)
        );
    }

    @DisplayName("예약 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void deleteReservationById() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");

        createReservation("brown", LocalDate.of(2026, 5, 15), 1L, 1L);

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationService.deleteReservationById(1L),
                100,
                ReservationNotFoundException.class
        );

        //then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1),
                () -> assertThat(result.get(1)).isEqualTo(99),
                () -> assertThat(result.get(2)).isEqualTo(0)
        );
    }

    @DisplayName("인가를 포함하는 예약 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void deleteReservationByIdWithAuthorization() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");

        createReservation("brown", LocalDate.of(2026, 5, 15), 1L, 1L);

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationService.deleteReservationById(1L, "brown"),
                100,
                ReservationNotFoundException.class
        );

        //then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1),
                () -> assertThat(result.get(1)).isEqualTo(99),
                () -> assertThat(result.get(2)).isEqualTo(0)
        );
    }

    @DisplayName("테마 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void removeThemeById() throws InterruptedException {
        //given
        createTheme("테마", "설명", "thumbnailUrl");

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> themeService.removeThemeById(1L),
                100,
                ThemeNotFoundException.class
        );

        //then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1),
                () -> assertThat(result.get(1)).isEqualTo(99),
                () -> assertThat(result.get(2)).isEqualTo(0)
        );
    }

    @DisplayName("예약 시간 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void removeReservationTimeById() throws InterruptedException {
        //given
        createReservationTime("10:00");

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationTimeService.removeReservationTimeById(1L),
                100,
                TimeNotFoundException.class
        );

        //then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1),
                () -> assertThat(result.get(1)).isEqualTo(99),
                () -> assertThat(result.get(2)).isEqualTo(0)
        );
    }

    @DisplayName("서로 다른 본인 예약을 같은 슬롯으로 동시에 수정하면 하나만 성공하고 하나는 중복 예외가 발생한다")
    @Test
    void updateMyReservation() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createReservationTime("11:00");
        createReservationTime("12:00");
        createTheme("테마", "설명", "thumbnailUrl");

        Long reservationId1 = createReservation("brown", LocalDate.of(2026, 5, 15), 1L, 1L);
        Long reservationId2 = createReservation("coney", LocalDate.of(2026, 5, 15), 2L, 1L);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        AtomicInteger unexpectedErrorCount = new AtomicInteger();

        //when
        List<Runnable> tasks = List.of(
                () -> reservationService.updateReservation(
                        new ReservationUpdateCommand(LocalDate.of(2026, 5, 16), 3L),
                        reservationId1,
                        "brown"
                ),
                () -> reservationService.updateReservation(
                        new ReservationUpdateCommand(LocalDate.of(2026, 5, 16), 3L),
                        reservationId2,
                        "coney"
                )
        );

        for (Runnable task : tasks) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    task.run();
                    successCount.incrementAndGet();
                } catch (DuplicateReservationException e) {
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
        assertAll(
                () -> assertThat(successCount.get()).isEqualTo(1),
                () -> assertThat(duplicateCount.get()).isEqualTo(1),
                () -> assertThat(unexpectedErrorCount.get()).isEqualTo(0)
        );
    }

    private Long createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", name);
        reservation.put("date", date.toString());
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    @DisplayName("동일한 예약 대기 신청이 동시에 들어오면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void makeReservationWaiting() throws InterruptedException {
        //given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        createReservation("brown", LocalDate.of(2026, 5, 15), 1L, 1L);

        //when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationWaitingService.makeReservationWaiting(new ReservationWaitingCommand(
                                "name",
                                LocalDate.of(2026, 5, 15),
                                1L,
                                1L
                        )
                ),
                100,
                DuplicateReservationWaitingException.class
        );

        //then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1),
                () -> assertThat(result.get(1)).isEqualTo(99),
                () -> assertThat(result.get(2)).isEqualTo(0)
        );
    }

    @Test
    void deleteReservationWaiting() throws InterruptedException {
        // given
        createReservationTime("10:00");
        createTheme("테마", "설명", "thumbnailUrl");
        createReservation("brown", LocalDate.of(2026, 5, 15), 1L, 1L);

        reservationWaitingService.makeReservationWaiting(new ReservationWaitingCommand(
                "pobi",
                LocalDate.of(2026, 5, 15),
                1L,
                1L
        ));

        // when
        List<Integer> result = runConcurrentlyAndCountResults(
                () -> reservationWaitingService.deleteReservationWaitingById(1L),
                100,
                ReservationWaitingNotFoundException.class
        );

        // then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1),
                () -> assertThat(result.get(1)).isEqualTo(99),
                () -> assertThat(result.get(2)).isEqualTo(0)
        );
    }
}
