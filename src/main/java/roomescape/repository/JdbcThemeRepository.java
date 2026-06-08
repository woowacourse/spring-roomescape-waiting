package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcThemeRepository implements ThemeRepository {
    public static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) ->
            Theme.load(rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail_url"));

    private static final String BASE_SQL = "SELECT id, name, description, thumbnail_url FROM THEME";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
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
        return jdbcTemplate.query(BASE_SQL, THEME_ROW_MAPPER);
    }

    public List<Theme> findFamous(long days, LocalDate date, long limit) {
        LocalDate startDate = date.minusDays(days);
        LocalDate endDate = date.minusDays(1);

        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail_url
                FROM THEME AS t
                INNER JOIN (
                    SELECT theme_id, count(theme_id) AS cnt
                    FROM RESERVATION
                    WHERE date BETWEEN :startDate AND :endDate
                    GROUP BY theme_id
                    ORDER BY count(theme_id) DESC, theme_id DESC
                    LIMIT :limit
                ) AS topN ON t.id = topN.theme_id
                ORDER BY topN.cnt DESC, topN.theme_id DESC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, THEME_ROW_MAPPER);
    }

    public void deleteById(long themeId) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", themeId);
        jdbcTemplate.update("DELETE FROM theme WHERE id = :id", param);
    }

    public boolean existsById(long themeId) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", themeId);
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        "SELECT EXISTS (SELECT 1 FROM theme WHERE id = :id)",
                        param,
                        Boolean.class)
        );
    }

    public Optional<Theme> findById(long themeId) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", themeId);
        List<Theme> result = jdbcTemplate.query(BASE_SQL + " WHERE id = :id", param, THEME_ROW_MAPPER);
        return result.stream().findFirst();
    }
}
