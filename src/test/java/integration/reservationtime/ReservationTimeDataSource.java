package integration.reservationtime;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.TimeStatus;

@TestComponent
public class ReservationTimeDataSource {

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

    public void insertOneTheme() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "이프의 집", "이프의 집임", "http://image.png/image.com");
    }

    public void insertTimeByStartToEndWithOneHourLotation(int startHour, int endHour) {
        String sql = "INSERT INTO reservation_time (start_at, status) VALUES (?, ?)";
        for (int i = startHour; i <= endHour; i++) {
            jdbcTemplate.update(sql, LocalTime.of(i, 0), TimeStatus.ACTIVE.toString());
        }
    }

    public void insertReservation(long themeId, LocalDate date, long timeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation_slot (date, theme_id, time_id) VALUES (?, ?, ?)",
                date, themeId, timeId
        );
        Long reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_slot", Long.class);
        jdbcTemplate.update(
                """
                INSERT INTO reservation (name, slot_id, status, created_at)
                VALUES (?, ?, 'RESERVED', CURRENT_TIMESTAMP)
                """,
                "이프", reservationId
        );
    }
}
