package roomescape.infra;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Theme> themeRowMapper = (rs, rowNum) -> new Theme(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getString("thumbnail_image_url")
    );

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail_image_url " +
            "FROM theme ORDER BY id DESC";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        List<Theme> results = jdbcTemplate.query(sql, themeRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public List<Theme> getPopularTop10Themes(LocalDate start, LocalDate end) {
        String sql = """
            SELECT
            t.id, t.name, t.description, t.thumbnail_image_url
            FROM reservation as r
            JOIN slot as s ON r.slot_id = s.id
            JOIN theme as t ON s.theme_id = t.id
            WHERE s.date BETWEEN ? AND ?
            GROUP BY t.id, t.name, t.description, t.thumbnail_image_url
            ORDER BY COUNT(*) DESC
            LIMIT 10;
            """;
        return jdbcTemplate.query(
            sql,
            themeRowMapper,
            start,
            end
        );

    }

    @Override
    public Long save(Theme theme) {
        String sql = "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnailImageUrl());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
