package roomescape.test_util;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcTestUtil {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTestUtil(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertUser(final String id, final String name) {
        final String sql = "INSERT INTO users (id, name, email, password, role) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, name, name + "@email.com", "password123", "USER");
    }

    public void insertReservation(final String id, final LocalDate date, final String timeId, final String themeId, final String userId) {
        final String sql = "INSERT INTO reservation (id, date, time_id, theme_id, user_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, Date.valueOf(date), timeId, themeId, userId);
    }

    public void insertReservationTime(final String id, final LocalTime time) {
        final String sql = "INSERT INTO reservation_time (id, start_at) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, Time.valueOf(time));
    }

    public void insertTheme(final String id, final String name) {
        final String sql = "INSERT INTO theme (id, name, description, thumbnail) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, name, "", "");
    }

    public int countReservation() {
        final String sql = "SELECT COUNT(*) FROM reservation WHERE id IS NOT NULL";
        final Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return Objects.requireNonNull(count);
    }

    public int countReservationTime() {
        final String sql = "SELECT COUNT(*) FROM reservation_time WHERE id IS NOT NULL";
        final Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return Objects.requireNonNull(count);
    }

    public int countTheme() {
        final String sql = "SELECT COUNT(*) FROM theme WHERE id IS NOT NULL";
        final Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return Objects.requireNonNull(count);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM reservation WHERE id IS NOT NULL");
        jdbcTemplate.update("DELETE FROM users WHERE id IS NOT NULL");
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id IS NOT NULL");
        jdbcTemplate.update("DELETE FROM theme WHERE id IS NOT NULL");
    }
}
