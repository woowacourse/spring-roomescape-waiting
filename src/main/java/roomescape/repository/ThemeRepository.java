package roomescape.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.FamousThemeCondition;
import roomescape.domain.theme.Theme;

@Repository
public class ThemeRepository {
    public static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> RepositoryRowMapper.themeRowMapper(rs);

    private final JdbcTemplate jdbcTemplate;

    public ThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Theme> findFamous(FamousThemeCondition condition) {
        String sql = """
                SELECT t.id AS theme_id, t.name AS theme_name, t.description, t.thumbnail_url
                FROM THEME AS t
                INNER JOIN (
                    SELECT s.theme_id, count(s.theme_id) AS cnt
                    FROM reservation r
                    INNER JOIN slot s ON r.slot_id = s.id
                    WHERE s.date BETWEEN ? AND ?
                    GROUP BY s.theme_id
                    ORDER BY count(s.theme_id) DESC, s.theme_id DESC
                    LIMIT ?
                ) AS topN ON t.id = topN.theme_id
                ORDER BY topN.cnt DESC, topN.theme_id DESC
                """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER, condition.startDate(), condition.endDate(),
                condition.getLimit());
    }
}
