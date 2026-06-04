package roomescape.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.FamousThemeCondition;
import roomescape.domain.theme.Theme;

@Repository
public class ThemeRepository {
    public static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> RepositoryRowMapper.themeRowMapper(rs);
    private static final String EXISTS_BY_ID = """
            SELECT EXISTS (
                SELECT 1
                    FROM theme
                    WHERE id = ?
                    )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme save(Theme theme) {
        Map<String, Object> params = Map.of(
                "name", theme.getName().getValue(),
                "description", theme.getDescription(),
                "thumbnail_url", theme.getThumbnailUrl().getValue()
        );
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return theme.withId(generatedKey);
    }

    public List<Theme> findAll() {
        String sql = "SELECT id AS theme_id, name AS theme_name, description, thumbnail_url FROM THEME";
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
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

    public void deleteById(long themeId) {
        String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, themeId);
    }

    public boolean existsById(long themeId) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(EXISTS_BY_ID, Boolean.class, themeId));
    }

    public Optional<Theme> findById(long themeId) {
        String sql = "SELECT id AS theme_id, name AS theme_name, description, thumbnail_url FROM THEME WHERE id = ?";
        List<Theme> result = jdbcTemplate.query(sql, THEME_ROW_MAPPER, themeId);
        return result.stream().findFirst();
    }
}
