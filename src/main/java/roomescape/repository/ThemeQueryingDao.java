package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ThemeQueryingDao {

    private final JdbcTemplate jdbcTemplate;

    public ThemeQueryingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("url")
    );

    public Optional<Theme> findThemeById(long id) {
        String sql = """
                SELECT id, name, description, url
                FROM theme
                WHERE id = ?
                """;
        return jdbcTemplate.query(sql, themeRowMapper, id).stream()
                .findFirst();
    }

    public List<Theme> findAllTheme() {
        String sql = """
                SELECT id, name, description, url
                FROM theme
                """;
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    public List<Theme> findAllByTopTheme() {
        String sql = """
                SELECT t.id, t.name, t.description, t.url
                FROM theme as t
                INNER JOIN (
                    SELECT s.theme_id
                    FROM reservation as r
                    JOIN slot as s ON r.slot_id = s.id
                    WHERE r.created_at >= ?
                    GROUP BY s.theme_id
                    ORDER BY count(1) DESC
                    LIMIT 10
                ) AS top_themes ON t.id = top_themes.theme_id;
                """;
        LocalDateTime filtered = LocalDateTime.now().minusWeeks(1);
        return jdbcTemplate.query(sql, themeRowMapper, filtered);
    }
}
