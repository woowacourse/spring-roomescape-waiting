package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ThemeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<Theme> rowMapper = (rs, rowNum) -> Theme.from(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("thumbnail_url"),
            rs.getString("description")
    );

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Long create(Theme theme) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getThemeName())
                .addValue("thumbnail_url", theme.getThumbnailUrl())
                .addValue("description", theme.getDescription());

        Number themeId = insertExecutor.executeAndReturnKey(params);

        String sql = """
                SELECT * FROM theme WHERE theme.id = ?
                """;

        return themeId.longValue();
    }

    public Optional<Theme> findById(Long themeId) {
        String sql = """
                SELECT * FROM theme WHERE id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, themeId)
                .stream()
                .findFirst();
    }

    public List<Theme> findAllThemes() {
        String sql = """
                SELECT * FROM theme
                """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Theme> findSortedPopularThemesBy(LocalDate startAt, LocalDate endAt, int limit) {
        String sql = """
                SELECT 
                    theme.id, 
                    theme.name, 
                    theme.thumbnail_url, 
                    theme.description,
                    COUNT(reservation.id) as count
                FROM reservation
                INNER JOIN theme ON reservation.theme_id = theme.id
                WHERE reservation.date BETWEEN ? AND ?
                GROUP BY theme.id
                ORDER BY COUNT(reservation.id) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, rowMapper, startAt, endAt, limit);
    }

    public void delete(Theme theme) {
        String sql = """
                DELETE FROM theme WHERE id = ?
                """;
        jdbcTemplate.update(sql, theme.getId());
    }
}
