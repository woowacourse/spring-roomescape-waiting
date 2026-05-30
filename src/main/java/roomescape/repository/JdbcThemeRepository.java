package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    private static RowMapper<Theme> getThemeRowMapper() {
        return (resultSet, rowNum) -> Theme.of(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("image_url")
        );
    }

    @Override
    public Theme save(Theme theme) {
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(
                new BeanPropertySqlParameterSource(theme)
        ).longValue();

        return Theme.of(
                generatedKey,
                theme.getName(),
                theme.getDescription(),
                theme.getImageUrl()
        );
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = """
                    SELECT id,
                           name,
                           description,
                           image_url
                    FROM theme
                    WHERE id = :id
                """;

        Map<String, Object> params = Map.of("id", id);

        List<Theme> themes = jdbcTemplate.query(
                sql,
                params,
                getThemeRowMapper()
        );
        return themes.stream()
                .findFirst();
    }

    @Override
    public List<Theme> findAll() {
        String sql = """
                    SELECT id,
                           name,
                           description,
                           image_url
                    FROM theme
                """;

        return jdbcTemplate.query(
                sql,
                getThemeRowMapper()
        );
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, Long limit) {
        String sql = """
                    SELECT t.id AS theme_id,
                           t.name,
                           t.description,
                           t.image_url,
                           COUNT(r.id) AS reservation_count
                    FROM theme AS t
                    LEFT JOIN reservation AS r
                      ON r.theme_id = t.id
                      AND r.date >= :startDate
                      AND r.date < :endDate
                    GROUP BY t.id
                    ORDER BY reservation_count DESC,
                             t.name ASC
                    LIMIT :limit
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("limit", limit);

        return jdbcTemplate.query(
                sql,
                params,
                getThemeRowMapper()
        );
    }

    @Override
    public void delete(Long id) {
        String sql = """
                    DELETE FROM theme
                    WHERE id = :id
                """;
        Map<String, Object> params = Map.of("id", id);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean existByThemeName(String name) {
        String sql = """
                    SELECT EXISTS (
                      SELECT 1
                      FROM theme
                      WHERE name = :name
                    )
                """;
        Map<String, Object> params = Map.of("name", name);
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, params, Boolean.class)
        );
    }
}


