package roomescape.support;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationTestHelper {

    private final JdbcTemplate jdbcTemplate;

    public ReservationTestHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insertTime(LocalTime startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(startAt));
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?",
                Long.class, Time.valueOf(startAt));
    }

    public Long insertTheme(String name, String description, String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, description, thumbnailUrl);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class, name);
    }

    public void insertReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, Date.valueOf(date), timeId, themeId);
    }

    public Long insertReservationAndReturnId(String name, LocalDate date, Long timeId, Long themeId) {
        insertReservation(name, date, timeId, themeId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?",
                Long.class, name, Date.valueOf(date), timeId, themeId);
    }

    public Long insertWaiting(String name, LocalDate date, Long timeId, Long themeId, int order) {
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, order_index) VALUES (?, ?, ?, ?, ?)",
                name, date.toString(), timeId, themeId, order);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM waiting WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?",
                Long.class, name, date.toString(), timeId, themeId);
    }

    public String findReservationOwner(LocalDate date, Long timeId, Long themeId) {
        return jdbcTemplate.queryForObject(
                "SELECT name FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                String.class, date.toString(), timeId, themeId);
    }

    public boolean existsWaiting(Long waitingId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE id = ?", Integer.class, waitingId);
        return count != null && count > 0;
    }

    public int findWaitingOrder(Long waitingId) {
        return jdbcTemplate.queryForObject(
                "SELECT order_index FROM waiting WHERE id = ?", Integer.class, waitingId);
    }

    public int findReservationCount(LocalDate date, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class, date.toString(), timeId, themeId);
        return count == null ? 0 : count;
    }
}
