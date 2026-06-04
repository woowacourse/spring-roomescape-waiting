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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.exception.DuplicateReservationWaitingException;
import roomescape.reservationWaiting.exception.ReservationWaitingNotFoundException;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.support.ConcurrentExecutor;
import roomescape.support.ConcurrentResult;
import roomescape.support.ServiceIntegrationTest;

public class ReservationWaitingServiceIntegrationTest extends ServiceIntegrationTest {

    private static final long RESERVATION_ID = 1L;
    private static final long WAITING_ID = 1L;

    @Autowired
    JdbcTemplate jdbcTemplate;

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
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

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

    private void saveReservationTime(LocalTime startAt) {
        jdbcTemplate.update("""
                INSERT INTO reservation_time (start_at)
                VALUES (?)
                """, startAt);
    }

    private void saveTheme(String name, String description, String thumbnailUrl) {
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, thumbnail_url)
                VALUES (?, ?, ?)
                """, name, description, thumbnailUrl);
    }

    private void saveReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update("""
                INSERT INTO reservation (name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """, name, date, timeId, themeId);
    }


    @DisplayName("예약 대기 생성 중에는 동일 슬롯의 예약을 삭제/변경할 수 없다.")
    @Test
    void makeReservationWaitingTest_update_lock() throws Exception {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveReservationTime(LocalTime.of(11, 0));

        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

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
                    () -> assertWaitingCount(WAITING_ID, 0)
            );
        } finally {
            executorService.shutdownNow();
        }
    }

    @DisplayName("예약 대기 생성 중에는 동일 슬롯의 예약을 삭제할 수 없다.")
    @Test
    void makeReservationWaitingTest_delete_lock() throws Exception {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

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
                    () -> assertWaitingCount(WAITING_ID, 0)
            );
        } finally {
            executorService.shutdownNow();
        }
    }

    private void assertReservationName(LocalDate date, Long timeId, Long themeId, String expectedName) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT name FROM reservation WHERE reservation_date = ? AND time_id = ? AND theme_id = ?",
                String.class,
                date,
                timeId,
                themeId
        )).isEqualTo(expectedName);
    }

    private void assertWaitingCount(Long id, int expectedCount) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_waiting WHERE id = ?",
                Integer.class,
                id
        )).isEqualTo(expectedCount);
    }

    @DisplayName("예약 대기 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void deleteReservationWaitingTest_duplicate() throws InterruptedException {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");
        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);
        saveReservationWaiting("pobi", LocalDate.of(2026, 5, 5), 1L, 1L);

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

    private void saveReservationWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update("""
                INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """, name, date, timeId, themeId);
    }}
