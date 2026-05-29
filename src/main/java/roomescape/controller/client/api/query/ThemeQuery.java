package roomescape.controller.client.api.query;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.controller.client.api.dto.response.ThemeResponse;

@Component
@RequiredArgsConstructor
public class ThemeQuery {

    private static final RowMapper<ThemeResponse> THEME_RESPONSE_MAPPER = (rs, rowNum) ->
            new ThemeResponse(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail_image_url")
            );

    private final JdbcTemplate jdbcTemplate;

    public List<ThemeResponse> getAllActiveThemes() {
        String sql = "SELECT id, name, description, thumbnail_image_url FROM theme WHERE is_active = 1";
        return jdbcTemplate.query(sql, THEME_RESPONSE_MAPPER);
    }

    public List<ThemeResponse> getPopularThemes(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    t.id AS id,
                    t.name AS name,
                    t.description AS description,
                    t.thumbnail_image_url AS thumbnail_image_url
                FROM theme t
                LEFT JOIN reservation_slot r
                       ON t.id = r.theme_id
                      AND r.date BETWEEN ? AND ?
                WHERE t.is_active = 1
                GROUP BY t.id, t.name, t.description, t.thumbnail_image_url
                ORDER BY COUNT(r.id) DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, THEME_RESPONSE_MAPPER, startDate, endDate);
    }
}
