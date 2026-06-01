package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Theme(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("thumbnail")));
    }

    @Override
    public Optional<Theme> findById(long themeId) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        try {
            Theme theme = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                String thumbnail = rs.getString("thumbnail");

                return new Theme(id, name, description, thumbnail);
            }, themeId);
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Long> findReservedTimeIds(long themeId, LocalDate date) {
        String sql = "SELECT r.time_id FROM reservation r " +
                "JOIN reservation_time rt ON r.time_id = rt.id " +
                "WHERE r.theme_id = ? AND r.date = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getLong("time_id"),
                themeId, date);
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = "SELECT r.theme_id, t.name, t.description, t.thumbnail " +
                "FROM reservation r " +
                "JOIN theme t ON r.theme_id = t.id " +
                "WHERE r.date BETWEEN ? AND ? " +
                "GROUP BY r.theme_id " +
                "ORDER BY count(r.theme_id) DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Theme(
                rs.getLong("theme_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("thumbnail")),
                startDate, endDate, limit);
    }

    @Override
    public Theme save(Theme theme) {
        String sql = "INSERT INTO theme (name, description, thumbnail) VALUES (?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnail());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
