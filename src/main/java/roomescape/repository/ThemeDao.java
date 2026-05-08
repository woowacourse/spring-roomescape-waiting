package roomescape.repository;

import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

import java.util.List;
import java.util.NoSuchElementException;

@Repository
public class ThemeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<Theme> rowMapper = (rs, rowNum) -> Theme.create(
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

    public Theme save(String name, String thumbnailUrl, String description) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("thumbnail_url", thumbnailUrl)
                .addValue("description", description);

        Number themeId = insertExecutor.executeAndReturnKey(params);

        String sql = """
                SELECT * FROM theme WHERE theme.id = ?
                """;

        return jdbcTemplate.queryForObject(sql, rowMapper, themeId.longValue());
    }

    public void delete(long themeId) {
        String sql = "DELETE FROM theme WHERE id = ?";
        int affected = jdbcTemplate.update(sql, themeId);

        if(affected == 0) {
            throw new NoSuchElementException("[ERROR] 삭제할 id에 해당하는 테마가 존재하지 않습니다.");
        }
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
}
