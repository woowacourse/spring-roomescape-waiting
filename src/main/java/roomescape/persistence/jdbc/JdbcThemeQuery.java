package roomescape.persistence.jdbc;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.controller.client.api.dto.response.ThemeResponse;
import roomescape.controller.client.api.query.ThemeQuery;
import roomescape.persistence.jdbc.mapper.ThemeResponseRowMapper;

@Component
@RequiredArgsConstructor
public class JdbcThemeQuery implements ThemeQuery {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ThemeResponse> getAllActiveThemes() {
        String sql = "SELECT id, name, description, thumbnail_image_url, price FROM theme WHERE is_active = 1 ORDER BY id ASC";
        return jdbcTemplate.query(sql, ThemeResponseRowMapper.THEME_RESPONSE_ROW_MAPPER);
    }

    @Override
    public List<ThemeResponse> getPopularThemes(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    t.id AS id,
                    t.name AS name,
                    t.description AS description,
                    t.thumbnail_image_url AS thumbnail_image_url,
                    t.price AS price
                FROM theme t
                LEFT JOIN reservation_slot r
                       ON t.id = r.theme_id
                      AND r.date BETWEEN ? AND ?
                WHERE t.is_active = 1
                GROUP BY t.id, t.name, t.description, t.thumbnail_image_url, t.price
                ORDER BY COUNT(r.id) DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, ThemeResponseRowMapper.THEME_RESPONSE_ROW_MAPPER, startDate, endDate);
    }
}
