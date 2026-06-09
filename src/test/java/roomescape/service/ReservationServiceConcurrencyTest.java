package roomescape.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.Reservation;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql({"/schema.sql", "/test-data.sql"})
class ReservationServiceConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    @DisplayName("서로 다른 슬롯의 대기 예약을 동시에 교차 변경해도 제한 시간 안에 완료된다.")
    void modifyReservationsAcrossThemeSlotsConcurrently() throws Exception {
        long firstThemeSlotId = insertThemeSlot(LocalDate.now().plusDays(30), 1L);
        long secondThemeSlotId = insertThemeSlot(LocalDate.now().plusDays(31), 2L);
        insertReservation("첫확정", "CONFIRMED", firstThemeSlotId);
        insertReservation("둘확정", "CONFIRMED", secondThemeSlotId);
        long firstReservationId = insertReservation("브라운", "PENDING", firstThemeSlotId);
        long secondReservationId = insertReservation("네오", "PENDING", secondThemeSlotId);

        CountDownLatch lockedReservations = new CountDownLatch(2);
        Future<Reservation> firstFuture = modifyReservationAfterLockAsync(
                lockedReservations,
                firstReservationId,
                secondThemeSlotId
        );
        Future<Reservation> secondFuture = modifyReservationAfterLockAsync(
                lockedReservations,
                secondReservationId,
                firstThemeSlotId
        );

        firstFuture.get(5, TimeUnit.SECONDS);
        secondFuture.get(5, TimeUnit.SECONDS);

        assertThat(findThemeSlotId(firstReservationId)).isEqualTo(secondThemeSlotId);
        assertThat(findThemeSlotId(secondReservationId)).isEqualTo(firstThemeSlotId);
        assertThat(findStatus(firstReservationId)).isEqualTo("PENDING");
        assertThat(findStatus(secondReservationId)).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("확정 예약 취소와 첫 번째 대기 예약 변경이 동시에 실행되어도 제한 시간 안에 완료된다.")
    void cancelConfirmedReservationAndModifyFirstPendingReservationConcurrently() throws Exception {
        long firstThemeSlotId = insertThemeSlot(LocalDate.now().plusDays(30), 1L);
        long secondThemeSlotId = insertThemeSlot(LocalDate.now().plusDays(31), 2L);
        long confirmedReservationId = insertReservation("확정", "CONFIRMED", firstThemeSlotId);
        long firstPendingReservationId = insertReservation("첫대기", "PENDING", firstThemeSlotId);

        CountDownLatch lockedReservations = new CountDownLatch(2);
        Future<Void> cancelFuture = cancelReservationAfterLockAsync(
                lockedReservations,
                confirmedReservationId,
                "확정"
        );
        Future<Reservation> modifyFuture = modifyReservationAfterLockAsync(
                lockedReservations,
                firstPendingReservationId,
                secondThemeSlotId
        );

        cancelFuture.get(5, TimeUnit.SECONDS);
        modifyFuture.get(5, TimeUnit.SECONDS);

        assertThat(findStatus(confirmedReservationId)).isEqualTo("CANCELLED");
        assertThat(findThemeSlotId(firstPendingReservationId)).isEqualTo(secondThemeSlotId);
    }

    private Future<Void> cancelReservationAfterLockAsync(
            CountDownLatch lockedReservations,
            long reservationId,
            String name
    ) {
        return executorService.submit(() -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.executeWithoutResult(status -> {
                lockReservationForUpdate(reservationId);
                waitUntilAllReservationsLocked(lockedReservations);
                reservationService.cancelReservation(reservationId, name);
            });
            return null;
        });
    }

    private Future<Reservation> modifyReservationAfterLockAsync(
            CountDownLatch lockedReservations,
            long reservationId,
            long targetThemeSlotId
    ) {
        return executorService.submit(() -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            return transactionTemplate.execute(status -> {
                lockReservationForUpdate(reservationId);
                waitUntilAllReservationsLocked(lockedReservations);
                return reservationService.modifyReservation(reservationId, targetThemeSlotId);
            });
        });
    }

    private void lockReservationForUpdate(long reservationId) {
        jdbcTemplate.queryForObject("""
                        SELECT id
                        FROM reservation
                        WHERE id = ?
                        FOR UPDATE
                        """,
                Long.class,
                reservationId
        );
    }

    private void waitUntilAllReservationsLocked(CountDownLatch lockedReservations) {
        lockedReservations.countDown();
        try {
            assertThat(lockedReservations.await(1, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private long insertThemeSlot(LocalDate date, long timeId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO theme_slot (theme_id, date, time_id, is_reserved)
                    VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, 1L);
            ps.setObject(2, date);
            ps.setLong(3, timeId);
            ps.setBoolean(4, true);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long insertReservation(String name, String status, long themeSlotId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO reservation (name, status, theme_slot_id)
                    VALUES (?, ?, ?)
                    """, new String[]{"id"});
            ps.setString(1, name);
            ps.setString(2, status);
            ps.setLong(3, themeSlotId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long findThemeSlotId(long reservationId) {
        return jdbcTemplate.queryForObject("""
                        SELECT theme_slot_id
                        FROM reservation
                        WHERE id = ?
                        """,
                Long.class,
                reservationId
        );
    }

    private String findStatus(long reservationId) {
        return jdbcTemplate.queryForObject("""
                        SELECT status
                        FROM reservation
                        WHERE id = ?
                        """,
                String.class,
                reservationId
        );
    }
}
