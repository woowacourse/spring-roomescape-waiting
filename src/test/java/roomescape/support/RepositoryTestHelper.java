package roomescape.support;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class RepositoryTestHelper {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert themeInsert;
    private final SimpleJdbcInsert reservationTimeInsert;

    public RepositoryTestHelper(JdbcTemplate jdbcTemplate) {
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
                "thumbnail_img_url", thumbnailImgUrl,
                "price", 30000L
        )).longValue();
    }

    public Long insertReservationTime(LocalTime startAt) {
        return reservationTimeInsert.executeAndReturnKey(Map.of(
                "start_at", startAt
        )).longValue();
    }

    public void insertReservation(String name, LocalDate date, Long themeId, Long timeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id) VALUES (?, ?, ?, ?)",
                name,
                date,
                themeId,
                timeId
        );
    }
}
