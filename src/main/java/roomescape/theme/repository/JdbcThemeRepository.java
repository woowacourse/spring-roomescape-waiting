package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> THEME_ROW_MAPPER = themeRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    private static RowMapper<Theme> themeRowMapper() {
        return (resultSet, rowNum) -> Theme.of(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("image_url"),
                resultSet.getLong("running_time")
        );
    }

    @Override
    public Theme save(Theme theme) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("image_url", theme.getImageUrl())
                .addValue("running_time", theme.getRunningTime());
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Theme.of(
                generatedKey,
                theme.getName(),
                theme.getDescription(),
                theme.getImageUrl(),
                theme.getRunningTime()
        );
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = """
                    SELECT id,
                           name,
                           description,
                           image_url,
                           running_time
                    FROM theme
                    WHERE id = :id
                """;

        List<Theme> themes = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("id", id),
                THEME_ROW_MAPPER
        );
        return themes.stream().findFirst();
    }

    @Override
    public List<Theme> findAll() {
        String sql = """
                    SELECT id,
                           name,
                           description,
                           image_url,
                           running_time
                    FROM theme
                """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, Long limit) {
        String sql = """
                    SELECT t.id,
                           t.name,
                           t.description,
                           t.image_url,
                           t.running_time,
                           COUNT(r.id) AS reservation_count
                    FROM theme AS t
                    LEFT JOIN slot AS s
                      ON s.theme_id = t.id
                      AND s.date >= :startDate
                      AND s.date < :endDate
                    LEFT JOIN reservation AS r
                      ON r.slot_id = s.id
                      AND r.status = 'CONFIRMED'
                    GROUP BY t.id, t.name, t.description, t.image_url, t.running_time
                    ORDER BY reservation_count DESC,
                             t.name ASC
                    LIMIT :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, THEME_ROW_MAPPER);
    }

    @Override
    public void delete(Long id) {
        String sql = """
                    DELETE FROM theme
                    WHERE id = :id
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
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
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("name", name), Boolean.class)
        );
    }
}
