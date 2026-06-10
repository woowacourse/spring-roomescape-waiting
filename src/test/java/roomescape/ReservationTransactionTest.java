package roomescape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.service.ReservationService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationTransactionTest {

    private static final String 예약자 = "예약자";
    private static final String 대기자 = "대기자";

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    @DisplayName("정상 취소 시: 예약 삭제/대기 승격/대기 삭제가 모두 함께 반영된다")
    void 정상_취소시_세_변경이_원자적으로_반영된다() {
        String futureDate = LocalDate.now().plusDays(1).toString();
        long timeId = saveReservationTime("10:00");
        long themeId = saveTheme("공포");
        long reservationId = saveReservation(예약자, futureDate, timeId, themeId);
        saveWaiting(대기자, futureDate, timeId, themeId);

        reservationService.deleteUserReservation(reservationId, 예약자);

        assertThat(countReservations()).isEqualTo(1);
        assertThat(reservationExists(예약자)).isFalse();
        assertThat(reservationExists(대기자)).isTrue();
        assertThat(countWaitings()).isZero();
    }

    private long saveReservationTime(String startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt);
    }

    private long saveTheme(String name) {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                name, "무서운 테마", "thumb.png");
        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?", Long.class, name);
    }

    private long saveReservation(String name, String date, long timeId, long themeId) {
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE name = ?", Long.class, name);
    }

    private void saveWaiting(String name, String date, long timeId, long themeId) {
        jdbcTemplate.update("INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId);
    }

    private int countReservations() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
    }

    private int countWaitings() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM waiting", Integer.class);
    }

    private boolean reservationExists(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE name = ?", Integer.class, name);
        return count != null && count > 0;
    }
}
