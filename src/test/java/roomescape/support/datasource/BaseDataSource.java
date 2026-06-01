package roomescape.support.datasource;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseDataSource {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    public void clearTable() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("TRUNCATE TABLE theme");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    public void clearId() {
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
    }

    public void insertTheme(String name, String thumbnailImageUrl, String description) {
        jdbcTemplate.update("INSERT INTO theme (name, thumbnail_image_url, description) VALUES (?, ?, ?)",
                name, thumbnailImageUrl, description);
    }

    public void insertReservationTime(LocalTime reservationTime) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", reservationTime);
    }

    public void insertReservation(String name, LocalDate date, Long themeId, Long timeId, String status) {
        jdbcTemplate.update("""
                        INSERT INTO reservation (name, date, theme_id, time_id, status, created_at)
                        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                        """,
                name, date, themeId, timeId, status);
    }
}
