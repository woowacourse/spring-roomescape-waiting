package integration.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.TimeStatus;

@TestComponent
public class ReservationDataSource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void clearTable() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        jdbcTemplate.execute("TRUNCATE TABLE theme");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_slot");

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    public void clearId() {
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
    }

    public void insertTheme(String name, String description, String thumbnailImageUrl) {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                name, description, thumbnailImageUrl);
    }

    public void insertReservation(String name, LocalDate date, Long themeId, Long timeId) {
        jdbcTemplate.update("INSERT INTO reservation_slot (date, theme_id, time_id) VALUES (?, ?, ?)",
                date, themeId, timeId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_slot", Long.class);
        jdbcTemplate.update("""
                        INSERT INTO reservation (name, slot_id, status, created_at)
                        VALUES (?, ?, 'RESERVED', CURRENT_TIMESTAMP)
                        """,
                name, reservationId);
    }

    public void insertReservationTime(LocalTime reservationTime) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, status) VALUES (?, ?)", reservationTime,
                TimeStatus.ACTIVE.toString());
    }

    public boolean hasReservationById(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_slot WHERE id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }

    public int countReservations() {
        String sql = "SELECT COUNT(*) FROM reservation_slot";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
