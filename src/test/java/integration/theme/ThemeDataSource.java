package integration.theme;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.TimeStatus;

@TestComponent
public class ThemeDataSource {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void clearTable() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_slot");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time");
        jdbcTemplate.execute("TRUNCATE TABLE theme");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    public void clearId() {
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
    }

    public void insertReservation(String name, LocalDate date, Long themeId, Long timeId) {
        jdbcTemplate.update("INSERT INTO reservation_slot (date, theme_id, time_id) VALUES (?, ?, ?)",
                date, themeId, timeId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_slot", Long.class);
        insertReservation(name, reservationId);
    }

    public void insertThemesByCount(int count) {
        for (int i = 0; i < count; i++) {
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                    "테마" + i, "설명" + i, "https://image.com/image" + i + ".png");
        }
    }

    public void insertTimeByStartToEndWithOneHourRotation(int startHour, int endHour) {
        String sql = "INSERT INTO reservation_time (start_at, status) VALUES (?, ?)";
        for (int i = startHour; i <= endHour; i++) {
            jdbcTemplate.update(sql, LocalTime.of(i, 0), TimeStatus.ACTIVE.toString());
        }
    }

    public void insertReservationByTheme(long themeId, int reservationCount) {
        for (long timeId = 1L; timeId <= reservationCount; timeId++) {
            jdbcTemplate.update("INSERT INTO reservation_slot (date, theme_id, time_id) VALUES (?, ?, ?)",
                    LocalDate.now(), themeId, timeId);
            Long reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_slot", Long.class);
            insertReservation("바니", reservationId);
        }
    }

    private void insertReservation(String name, Long reservationId) {
        jdbcTemplate.update("""
                        INSERT INTO reservation (name, slot_id, status, active_status, created_at)
                        VALUES (?, ?, 'RESERVED', 'ACTIVE', CURRENT_TIMESTAMP)
                        """,
                name, reservationId);
    }
}
