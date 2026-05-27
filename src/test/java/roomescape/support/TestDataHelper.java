package roomescape.support;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.theme.application.dto.ThemeCreateCommand;

@TestComponent
public class TestDataHelper {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert themeInsert;
    private final SimpleJdbcInsert reservationTimeInsert;

    public TestDataHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.reservationTimeInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public Long insertTheme(String name, String description, String thumbnailImgUrl) {
        return themeInsert.executeAndReturnKey(Map.of(
                "name", name,
                "description", description,
                "thumbnail_img_url", thumbnailImgUrl
        )).longValue();
    }

    public Long insertTheme(ThemeCreateCommand command) {
        return insertTheme(command.name(), command.description(), command.thumbnailImgUrl());
    }

    public Long insertReservationTime(LocalTime startAt) {
        return reservationTimeInsert.executeAndReturnKey(Map.of(
                "start_at", startAt
        )).longValue();
    }

    public Long insertReservation(String name, LocalDate date, Long themeId, Long timeId) {
        SimpleJdbcInsert reservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
        return reservationInsert.executeAndReturnKey(Map.of(
                "name", name,
                "date", date,
                "theme_id", themeId,
                "time_id", timeId
        )).longValue();
    }

    public Long insertWaiting(String name, LocalDate date, Long themeId, Long timeId) {
        SimpleJdbcInsert waitingInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
        return waitingInsert.executeAndReturnKey(Map.of(
                "name", name,
                "date", date,
                "theme_id", themeId,
                "time_id", timeId
        )).longValue();
    }

}
