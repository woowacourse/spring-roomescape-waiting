package roomescape.support;

import java.time.LocalDate;
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
        jdbcTemplate.execute("TRUNCATE TABLE reservation_entry");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time");
        jdbcTemplate.execute("TRUNCATE TABLE theme");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    public void clearId() {
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_entry ALTER COLUMN id RESTART WITH 1");
    }

    public void insertReservation(String name, LocalDate date, Long themeId, Long timeId) {
        jdbcTemplate.update("INSERT INTO reservation (date, theme_id, time_id) VALUES (?, ?, ?)",
                date, themeId, timeId);
        Long reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        insertReservationEntry(name, reservationId);
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
            jdbcTemplate.update(sql, TestDateTimes.hour(i), TimeStatus.ACTIVE.toString());
        }
    }

    public void insertReservationByTheme(long themeId, int reservationCount) {
        for (long timeId = 1L; timeId <= reservationCount; timeId++) {
            jdbcTemplate.update("INSERT INTO reservation (date, theme_id, time_id) VALUES (?, ?, ?)",
                    TestDateTimes.today(), themeId, timeId);
            Long reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
            insertReservationEntry("바니", reservationId);
        }
    }

    private void insertReservationEntry(String name, Long reservationId) {
        jdbcTemplate.update("""
                        INSERT INTO reservation_entry (name, reservation_id, status, created_at)
                        VALUES (?, ?, 'RESERVED', CURRENT_TIMESTAMP)
                        """,
                name, reservationId);
    }
}
