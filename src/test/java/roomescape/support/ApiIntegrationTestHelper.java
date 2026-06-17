package roomescape.support;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class ApiIntegrationTestHelper {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert themeInsert;
    private final SimpleJdbcInsert reservationTimeInsert;
    private final SimpleJdbcInsert reservationInsert;
    private final SimpleJdbcInsert waitingInsert;

    public ApiIntegrationTestHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.reservationTimeInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.reservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "date", "theme_id", "time_id");
        this.waitingInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    public void clearDatabase() {
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    public Long insertTheme(String name, String description, String thumbnailImgUrl) {
        return themeInsert.executeAndReturnKey(Map.of(
                "name", name,
                "description", description,
                "thumbnail_img_url", thumbnailImgUrl,
                "price", 30000L
        )).longValue();
    }

    public Long insertReservationTime(LocalTime startAt) {
        return reservationTimeInsert.executeAndReturnKey(Map.of(
                "start_at", startAt
        )).longValue();
    }

    public Long insertReservation(String name, LocalDate date, Long themeId, Long timeId) {
        return reservationInsert.executeAndReturnKey(Map.of(
                "name", name,
                "date", date,
                "theme_id", themeId,
                "time_id", timeId
        )).longValue();
    }

    public Long insertWaiting(String name, LocalDate date, Long themeId, Long timeId) {
        return waitingInsert.executeAndReturnKey(Map.of(
                "name", name,
                "date", date,
                "theme_id", themeId,
                "time_id", timeId
        )).longValue();
    }
}
