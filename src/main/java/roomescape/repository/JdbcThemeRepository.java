package roomescape.repository;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.exception.ThemeNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    private static Map<String, Object> createParams(Theme theme) {
        return Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail_url", theme.getThumbnailUrl()
        );
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail_url FROM theme";
        return jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public Optional<Theme> findById(long id) {
        String sql = "SELECT id, name, description, thumbnail_url FROM theme WHERE id = ?";
        List<Theme> themes = jdbcTemplate.query(sql, rowMapper(), id);
        return Optional.ofNullable(DataAccessUtils.singleResult(themes));
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> params = createParams(theme);
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM theme where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Theme update(Theme theme) {
        String sql = "UPDATE theme SET name = ?, description = ?, thumbnail_url = ? WHERE id = ?";
        int columns = jdbcTemplate.update(sql, theme.getName(), theme.getDescription(), theme.getThumbnailUrl(),
                theme.getId());
        if (columns == 0) {
            throw new ThemeNotFoundException(theme.getId());
        }
        return theme;
    }

    @Override
    public List<Theme> findPopularThemes(Long topCount, LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url,
                    count(*) as reservation_count
                FROM (
                    SELECT *
                    FROM reservation
                    WHERE date BETWEEN ? AND ?
                ) as r
                
                INNER JOIN theme t
                ON r.theme_id = t.id
                GROUP BY t.id
                ORDER BY reservation_count DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                rowMapper(),
                fromDate,
                toDate,
                topCount
        );
    }

    private RowMapper<Theme> rowMapper() {
        return (rs, rowNum) -> new Theme(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("thumbnail_url")
        );
    }
}
