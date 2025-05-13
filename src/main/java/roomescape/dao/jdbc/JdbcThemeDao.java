package roomescape.dao.jdbc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;
import roomescape.exception.custom.NotFoundException;

@Repository
public class JdbcThemeDao implements ThemeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("theme")
            .usingGeneratedKeyColumns("id");
    }

    public List<Theme> findAllThemes() {
        String sql = "SELECT id, name, description, thumbnail FROM theme";
        return jdbcTemplate.query(sql, createThemeMapper());
    }

    public Theme findThemeById(Long id) {
        String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, createThemeMapper(), id);
        } catch (DataAccessException exception) {
            throw new NotFoundException("theme");
        }
    }

    public List<Theme> findTopReservedThemesInPeriodWithLimit(LocalDate startDate,
        LocalDate endDate, int limitCount) {
        String sql = """
                SELECT
                  t.id,
                  t.name,
                  t.description,
                  t.thumbnail
                FROM
                  reservation as r
                  INNER JOIN theme as t
                  ON r.theme_id = t.id
                WHERE
                  r.date >= ?
                  AND r.date < ?
                GROUP BY
                  theme_id
                ORDER BY
                  COUNT(theme_id) DESC
                LIMIT ?;
            """;
        return jdbcTemplate.query(sql, createThemeMapper(), startDate, endDate, limitCount);
    }

    public boolean existThemeByName(String name) {
        String sql = "SELECT EXISTS(SELECT id FROM theme WHERE name = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, name));
    }

    public Theme addTheme(Theme theme) {
        Map<String, Object> param = new HashMap<>();
        param.put("name", theme.getName());
        param.put("description", theme.getDescription());
        param.put("thumbnail", theme.getThumbnail());

        Number key = jdbcInsert.executeAndReturnKey(param);
        return new Theme(key.longValue(), theme.getName(), theme.getDescription(),
            theme.getThumbnail());
    }

    public void removeThemeById(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        int activeRow = jdbcTemplate.update(sql, id);

        if (activeRow == 0) {
            throw new NotFoundException("theme");
        }
    }

    private RowMapper<Theme> createThemeMapper() {
        return (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail"));
    }
}
