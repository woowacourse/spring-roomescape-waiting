package roomescape.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.repository.dto.TimeSlotProjection;
import roomescape.service.result.ThemeRegisterResult;
import roomescape.service.result.ThemeTimesResult;

@Repository
@RequiredArgsConstructor
public class ThemeQueryRepository {

    private static final RowMapper<ThemeRegisterResult> THEME_ROW_MAPPER = (rs, rowNum) ->
            new ThemeRegisterResult(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail_image_url"),
                    rs.getLong("price"),
                    rs.getBoolean("is_active")
            );

    private final JdbcTemplate jdbcTemplate;

    public List<ThemeRegisterResult> getAllThemes() {
        String sql = "SELECT id, name, description, thumbnail_image_url, price, is_active FROM theme";
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    public List<ThemeRegisterResult> getAllActiveThemes() {
        String sql = "SELECT id, name, description, thumbnail_image_url, price, is_active FROM theme WHERE is_active = 1";
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    public List<ThemeRegisterResult> getPopularThemes(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    t.id AS id,
                    t.name AS name,
                    t.description AS description,
                    t.thumbnail_image_url AS thumbnail_image_url,
                    t.price AS price,
                    t.is_active AS is_active
                FROM theme t
                LEFT JOIN reservation r
                       ON t.id = r.theme_id
                      AND r.date BETWEEN ? AND ?
                WHERE t.is_active = 1
                GROUP BY t.id, t.name, t.description, t.thumbnail_image_url, t.price, t.is_active
                ORDER BY COUNT(r.id) DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER, startDate, endDate);
    }

    public List<ThemeTimesResult> getThemeReservationStatus(long themeId, LocalDate date) {
        String sql = """
                SELECT
                    rt.id AS time_id,
                    rt.start_at AS time_start_at,
                    NOT EXISTS (
                        SELECT 1
                        FROM reservation r
                        JOIN reservation_entry re ON re.reservation_id = r.id
                        WHERE r.time_id = rt.id
                          AND r.theme_id = ?
                          AND r.date = ?
                          AND re.status = 'RESERVED'
                    ) AS is_reservable
                FROM reservation_time rt
                WHERE rt.status = 'ACTIVE'
                ORDER BY rt.start_at ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new TimeSlotProjection(
                        rs.getLong("time_id"),
                        rs.getTime("time_start_at").toLocalTime(),
                        rs.getBoolean("is_reservable")
                ), themeId, date)
                .stream()
                .map(projection -> toThemeTimesResult(projection, date))
                .toList();
    }

    private ThemeTimesResult toThemeTimesResult(TimeSlotProjection projection, LocalDate date) {
        if (LocalDateTime.of(date, projection.startAt()).isBefore(LocalDateTime.now())) {
            return ThemeTimesResult.unavailable(projection);
        }
        return ThemeTimesResult.from(projection);
    }
}
