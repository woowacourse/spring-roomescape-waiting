package roomescape.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

@Repository
public class ThemeDao {
    private static final RowMapper<Theme> ROW_MAPPER = (resultSet, rowNum) ->
            new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme insert(Theme theme) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", theme.getName());
        parameters.put("description", theme.getDescription());
        parameters.put("thumbnail", theme.getThumbnail());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new Theme(
                generatedId.longValue(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }

    public Optional<Theme> selectById(Long themeId) {
        String sql = """
                SELECT id, 
                       name, 
                       description,
                       thumbnail
                FROM theme
                WHERE id = ?""";

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, themeId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    public List<Theme> selectAll() {
        String sql = """
                SELECT id, 
                       name, 
                       description,
                       thumbnail
                FROM theme""";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<Theme> selectPopularThemesByPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT t.id,
                   t.name,
                   t.description,
                   t.thumbnail
            FROM reservation AS r
            INNER JOIN reservation_slot AS rs
            ON r.slot_id = rs.id
            INNER JOIN theme AS t
            ON rs.theme_id = t.id
            WHERE rs.date BETWEEN ? AND ?
            GROUP BY t.id, t.name, t.description, t.thumbnail
            ORDER BY COUNT(r.id) DESC
            LIMIT 10
            """;
        return jdbcTemplate.query(sql, ROW_MAPPER, startDate, endDate);
    }

    public boolean existsById(Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM theme
                    WHERE id = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, boolean.class, themeId);
    }

    public boolean existsByName(String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM theme
                    WHERE name = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, boolean.class, name);
    }

    public int delete(long themeId) {
        String sql = """
                DELETE FROM theme
                WHERE id = ?""";
        return jdbcTemplate.update(sql, themeId);
    }
}
