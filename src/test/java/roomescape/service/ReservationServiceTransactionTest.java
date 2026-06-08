package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Sql({"/schema.sql", "/test-data.sql"})
class ReservationServiceTransactionTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 저장 중 예외가 발생하면 슬롯 예약 상태 변경을 롤백한다.")
    void rollbackThemeSlotReservedWhenSaveReservationFails() {
        long themeSlotId = insertThemeSlot(LocalDate.now().plusDays(30));
        doThrow(new RuntimeException("예약 저장 실패"))
                .when(reservationRepository)
                .save(any(Reservation.class));

        assertThatThrownBy(() -> reservationService.saveReservation("브라운", themeSlotId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("예약 저장 실패");

        assertThat(findThemeSlotReserved(themeSlotId)).isFalse();
    }

    @Test
    @DisplayName("확정 예약을 다른 빈 슬롯으로 변경하면 기존 슬롯을 먼저 비운 뒤 첫 번째 대기 예약을 확정한다.")
    void promoteFirstPendingReservationAfterConfirmedReservationLeavesPreviousSlot() {
        long previousThemeSlotId = insertThemeSlot(LocalDate.now().plusDays(30), true);
        long targetThemeSlotId = insertThemeSlot(LocalDate.now().plusDays(31), false);
        long confirmedReservationId = insertReservation("브라운", "CONFIRMED", previousThemeSlotId);
        long pendingReservationId = insertReservation("김대기", "PENDING", previousThemeSlotId);

        reservationService.modifyReservation(confirmedReservationId, targetThemeSlotId);

        assertThat(findThemeSlotId(confirmedReservationId)).isEqualTo(targetThemeSlotId);
        assertThat(findStatus(confirmedReservationId)).isEqualTo("CONFIRMED");
        assertThat(findThemeSlotId(pendingReservationId)).isEqualTo(previousThemeSlotId);
        assertThat(findStatus(pendingReservationId)).isEqualTo("CONFIRMED");
        assertThat(countConfirmedReservations(previousThemeSlotId)).isEqualTo(1);
        assertThat(countConfirmedReservations(targetThemeSlotId)).isEqualTo(1);
    }

    @Test
    @DisplayName("예약 취소 후 대기 승격 중 예외가 발생하면 취소 상태 변경을 롤백한다.")
    void rollbackCancelledReservationWhenPromotingWaitingReservationFails() {
        long themeSlotId = insertThemeSlot(LocalDate.now().plusDays(30), true);
        long confirmedReservationId = insertReservation("브라운", "CONFIRMED", themeSlotId);
        long pendingReservationId = insertReservation("김대기", "PENDING", themeSlotId);
        doThrow(new RuntimeException("대기 승격 실패"))
                .when(reservationRepository)
                .updateStatus(any(Reservation.class), eq("PENDING"));

        assertThatThrownBy(() -> reservationService.cancelReservation(confirmedReservationId, "브라운"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("대기 승격 실패");

        assertThat(findStatus(confirmedReservationId)).isEqualTo("CONFIRMED");
        assertThat(findStatus(pendingReservationId)).isEqualTo("PENDING");
        assertThat(findThemeSlotReserved(themeSlotId)).isTrue();
    }

    private long insertThemeSlot(LocalDate date) {
        return insertThemeSlot(date, false);
    }

    private long insertThemeSlot(LocalDate date, boolean isReserved) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO theme_slot (theme_id, date, time_id, is_reserved)
                    VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, 1L);
            ps.setObject(2, date);
            ps.setLong(3, 1L);
            ps.setBoolean(4, isReserved);
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

    private boolean findThemeSlotReserved(long themeSlotId) {
        return jdbcTemplate.queryForObject("""
                        SELECT is_reserved
                        FROM theme_slot
                        WHERE id = ?
                        """,
                Boolean.class,
                themeSlotId
        );
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

    private int countConfirmedReservations(long themeSlotId) {
        return jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM reservation
                        WHERE theme_slot_id = ?
                        AND status = 'CONFIRMED'
                        """,
                Integer.class,
                themeSlotId
        );
    }
}
