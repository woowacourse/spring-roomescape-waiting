package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("url")
    );

    @Override
    public Optional<Theme> findThemeById(long id) {
        String sql = """
                SELECT id, name, description, url
                FROM theme
                WHERE id = ?
                """;
        return jdbcTemplate.query(sql, themeRowMapper, id).stream()
                .findFirst();
    }

    @Override
    public List<Theme> findAllTheme() {
        String sql = """
                SELECT id, name, description, url
                FROM theme
                """;
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
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

    @Override
    public Long insert(Theme theme) {
        String sql = "INSERT INTO theme (name, description, url) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getUrl());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
