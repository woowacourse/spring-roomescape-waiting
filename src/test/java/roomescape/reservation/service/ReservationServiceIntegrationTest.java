package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.exception.ReservationWaitingNotFoundException;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.service.ReservationWaitingService;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.support.ConcurrentExecutor;
import roomescape.support.ConcurrentResult;
import roomescape.support.ServiceIntegrationTest;

public class ReservationServiceIntegrationTest extends ServiceIntegrationTest {

    private static final long TIME_ID = 1L;
    private static final long RESERVATION_ID = 1L;
    private static final long WAITING_ID = 1L;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationWaitingService reservationWaitingService;

    @MockitoSpyBean
    ReservationRepository reservationRepository;

    @MockitoSpyBean
    ReservationWaitingRepository reservationWaitingRepository;

    @DisplayName("예약 변경 후 기존 슬롯의 대기 삭제가 실패하면 예약 변경이 롤백된다.")
    @Test
    void updateReservationTest_roll_back_when_waiting_delete_fails() {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);
        saveReservationWaiting("pobi", LocalDate.of(2026, 5, 5), 1L, 1L);

        doReturn(0)
                .when(reservationWaitingRepository)
                .deleteById(anyLong());

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 6), TIME_ID),
                RESERVATION_ID,
                "brown"
        )).isInstanceOf(ReservationWaitingNotFoundException.class);

        assertAll(
                () ->  assertReservationDate(RESERVATION_ID, LocalDate.of(2026, 5, 5)),
                () -> assertWaitingDate(WAITING_ID, LocalDate.of(2026, 5, 5))
        );
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

    private void saveReservationWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update("""
                INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """, name, date, timeId, themeId);
    }

    private void assertReservationDate(Long id, LocalDate date) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT reservation_date FROM reservation WHERE id = ?",
                Date.class,
                id
        ).toLocalDate()).isEqualTo(date);
    }

    private void assertWaitingDate(Long id, LocalDate date) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT reservation_date FROM reservation_waiting WHERE id = ?",
                Date.class,
                id
        ).toLocalDate()).isEqualTo(date);
    }

    @DisplayName("예약 변경 후 기존 슬롯의 대기 승격 저장이 실패하면 예약 변경과 대기 삭제가 롤백된다.")
    @Test
    void updateReservationTest_rolls_back_when_promotion_save_Fails() {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);
        saveReservationWaiting("pobi", LocalDate.of(2026, 5, 5), 1L, 1L);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(reservationRepository)
                .save(any(Reservation.class));

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 6), TIME_ID),
                RESERVATION_ID,
                "brown"
        )).isInstanceOf(DuplicateReservationException.class);

        assertAll(
                () -> assertReservationDate(RESERVATION_ID, LocalDate.of(2026, 5, 5)),
                () -> assertWaitingCount(WAITING_ID, 1)
        );
    }

    private void assertWaitingCount(Long id, int expectedCount) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_waiting WHERE id = ?", Integer.class, id
        )).isEqualTo(expectedCount);
    }

    @DisplayName("예약 삭제 후 기존 슬롯의 대기 삭제가 실패하면 예약 삭제가 롤백된다.")
    @Test
    void deleteReservationByIdTest_rolls_back_when_waiting_delete_fails() {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);
        saveReservationWaiting("pobi", LocalDate.of(2026, 5, 5), 1L, 1L);

        doReturn(0)
                .when(reservationWaitingRepository)
                .deleteById(anyLong());

        //when & then
        assertThatThrownBy(() -> reservationService.deleteReservationById(RESERVATION_ID))
                .isInstanceOf(ReservationWaitingNotFoundException.class);

        assertAll(
                () -> assertReservationCount(RESERVATION_ID, 1),
                () -> assertWaitingCount(WAITING_ID, 1)
        );
    }

    private void assertReservationCount(Long id, int expectedCount) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?", Integer.class, id
        )).isEqualTo(expectedCount);
    }

    @DisplayName("예약 삭제 후 기존 슬롯의 대기 승격 저장이 실패하면 예약 삭제와 대기 삭제가 롤백된다.")
    @Test
    void deleteReservationByIdTest_rolls_back_when_promotion_save_fails() {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);
        saveReservationWaiting("pobi", LocalDate.of(2026, 5, 5), 1L, 1L);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(reservationRepository)
                .save(any(Reservation.class));

        //when & then
        assertThatThrownBy(() -> reservationService.deleteReservationById(RESERVATION_ID))
                .isInstanceOf(DuplicateReservationException.class);

        assertAll(
                () -> assertReservationCount(RESERVATION_ID, 1),
                () -> assertWaitingCount(WAITING_ID, 1)
        );
    }

    @DisplayName("동일한 예약 요청이 동시에 들어오면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void makeReservationTest_duplicate() throws InterruptedException {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        //when
        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(100, () -> {
            try {
                reservationService.makeReservation(new ReservationCommand(
                        "brown",
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
                .allMatch(DuplicateReservationException.class::isInstance);
    }

    @DisplayName("서로 다른 본인 예약을 같은 슬롯으로 동시에 수정하면 하나만 성공하고 하나는 중복 예외가 발생한다")
    @Test
    void updateReservationTest_duplicate() throws InterruptedException {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveReservationTime(LocalTime.of(11, 0));
        saveReservationTime(LocalTime.of(12, 0));

        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);
        saveReservation("pobi", LocalDate.of(2026, 5, 5), 2L, 1L);

        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(List.of(
                () -> updateReservation(1L, "brown"),
                () -> updateReservation(2L, "pobi")
        ));

        //then
        assertThat(results).filteredOn(ConcurrentResult::success).hasSize(1);

        assertThat(results).filteredOn(result -> !result.success()).hasSize(1);
        assertThat(results)
                .filteredOn(result -> !result.success())
                .extracting(ConcurrentResult::exception)
                .allMatch(DuplicateReservationException.class::isInstance);
    }

    private ConcurrentResult updateReservation(Long reservationId, String name) {
        try {
            reservationService.updateReservation(
                    new ReservationUpdateCommand(LocalDate.of(2026, 5, 6), 3L),
                    reservationId,
                    name
            );

            return ConcurrentResult.withSuccess();
        } catch (Throwable e) {
            return ConcurrentResult.withFail(e);
        }
    }

    @DisplayName("예약 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다.")
    @Test
    void deleteReservationByIdTest_concurrent() throws InterruptedException {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

        //when
        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(100, () -> {
            try {
                reservationService.deleteReservationById(RESERVATION_ID);

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
                .allMatch(ReservationNotFoundException.class::isInstance);
    }

    @DisplayName("인가를 포함하는 예약 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void deleteReservationByIdTest_with_authorization_concurrent() throws InterruptedException {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

        //when
        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(100, () -> {
            try {
                reservationService.deleteReservationById(RESERVATION_ID, "brown");

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
                .allMatch(ReservationNotFoundException.class::isInstance);
    }

    @DisplayName("예약 삭제 중에는 승격 대상 예약 대기를 삭제할 수 없다.")
    @Test
    void deleteReservationByIdTest_waiting_delete_lock() throws Exception {
        //given
        saveReservationTime(LocalTime.of(10, 0));
        saveTheme("테마", "설명", "url");

        saveReservation("brown", LocalDate.of(2026, 5, 5), 1L, 1L);

        ReservationWaiting waiting = reservationWaitingService.makeReservationWaiting(
                new ReservationWaitingCommand(
                        "pobi",
                        LocalDate.of(2026, 5, 5),
                        1L,
                        1L
                )
        );

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        AtomicBoolean firstDelete = new AtomicBoolean(true);
        CountDownLatch promotionDeleteEntered = new CountDownLatch(1);
        CountDownLatch allowPromotionDelete = new CountDownLatch(1);

        doReturnWithDelayOnFirstWaitingDelete(firstDelete, promotionDeleteEntered, allowPromotionDelete);

        try {
            Future<?> deleteReservationFuture = executorService.submit(() ->
                    reservationService.deleteReservationById(RESERVATION_ID)
            );

            assertThat(promotionDeleteEntered.await(2, TimeUnit.SECONDS)).isTrue();

            Future<?> deleteWaitingFuture = executorService.submit(() ->
                    reservationWaitingService.deleteReservationWaitingById(waiting.getId(), "pobi")
            );

            Thread.sleep(200);
            assertThat(deleteWaitingFuture.isDone()).isFalse();

            //when
            allowPromotionDelete.countDown();
            deleteReservationFuture.get(2, TimeUnit.SECONDS);

            //then
            assertThatThrownBy(() -> deleteWaitingFuture.get(2, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(ReservationWaitingNotFoundException.class);

            assertAll(
                    () -> assertReservationName(LocalDate.of(2026, 5, 5), 1L, 1L, "pobi"),
                    () -> assertWaitingCount(waiting.getId(), 0)
            );
        } finally {
            executorService.shutdownNow();
        }
    }

    private void doReturnWithDelayOnFirstWaitingDelete(
            AtomicBoolean firstDelete,
            CountDownLatch promotionDeleteEntered,
            CountDownLatch allowPromotionDelete
    ) {
        doAnswer(invocation -> {
            if (firstDelete.compareAndSet(true, false)) {
                promotionDeleteEntered.countDown();
                assertThat(allowPromotionDelete.await(2, TimeUnit.SECONDS)).isTrue();
            }
            return invocation.callRealMethod();
        }).when(reservationWaitingRepository).deleteById(anyLong());
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
}
