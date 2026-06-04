package roomescape.support;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.TimeStatus;

@TestComponent
public class ReservationTimeDataSource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertOneTheme() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "이프의 집", "이프의 집임", "http://image.png/image.com");
    }

    public void insertTimeByStartToEndWithOneHourLotation(int startHour, int endHour) {
        String sql = "INSERT INTO reservation_time (start_at, status) VALUES (?, ?)";
        for (int i = startHour; i <= endHour; i++) {
            jdbcTemplate.update(sql, TestDateTimes.hour(i), TimeStatus.ACTIVE.toString());
        }
    }

    public void insertReservation(long themeId, LocalDate date, long timeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (date, theme_id, time_id) VALUES (?, ?, ?)",
                date, themeId, timeId
        );
        Long reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        jdbcTemplate.update(
                """
                        INSERT INTO reservation_entry (name, reservation_id, status, created_at)
                        VALUES (?, ?, 'RESERVED', CURRENT_TIMESTAMP)
                        """,
                "이프", reservationId
        );
    }
}
