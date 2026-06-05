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

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Future<Reservation> firstFuture = modifyReservationAsync(
                ready,
                start,
                firstReservationId,
                secondThemeSlotId
        );
        Future<Reservation> secondFuture = modifyReservationAsync(
                ready,
                start,
                secondReservationId,
                firstThemeSlotId
        );

        assertThat(ready.await(1, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        firstFuture.get(5, TimeUnit.SECONDS);
        secondFuture.get(5, TimeUnit.SECONDS);

        assertThat(findThemeSlotId(firstReservationId)).isEqualTo(secondThemeSlotId);
        assertThat(findThemeSlotId(secondReservationId)).isEqualTo(firstThemeSlotId);
        assertThat(findStatus(firstReservationId)).isEqualTo("PENDING");
        assertThat(findStatus(secondReservationId)).isEqualTo("PENDING");
    }

    private Future<Reservation> modifyReservationAsync(
            CountDownLatch ready,
            CountDownLatch start,
            long reservationId,
            long targetThemeSlotId
    ) {
        return executorService.submit(() -> {
            ready.countDown();
            start.await();
            return reservationService.modifyReservation(reservationId, targetThemeSlotId);
        });
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
