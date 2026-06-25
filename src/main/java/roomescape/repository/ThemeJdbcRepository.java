package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ThemeJdbcRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public ThemeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Theme> themeRowMapper = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_image_url")
    );

    public List<Theme> findAll() {
        String sql = """
                SELECT
                    id,
                    name,
                    description,
                    thumbnail_image_url
                FROM theme
                ORDER BY id DESC
                """;
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    public Theme save(Theme theme) {
        String sql = "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnailImageUrl());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnailImageUrl());
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }

    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        List<Theme> results = jdbcTemplate.query(sql, themeRowMapper, id);
        return results.stream().findFirst();
    }

    public List<Theme> getPopularThemes(LocalDate start, LocalDate end, Integer limit) {
        String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_image_url
                FROM theme as t
                JOIN reservation as r
                ON r.theme_id = t.id
                WHERE r.date BETWEEN ? AND ?
                AND r.reservation_status = 'CONFIRM'
                GROUP BY
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_image_url
                ORDER BY COUNT(*) DESC
                LIMIT ?;
                """;
        return jdbcTemplate.query(
                sql,
                themeRowMapper,
                start,
                end,
                limit
        );
    }
}
