package roomescape.persistence;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> themeRowMapper = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail"));

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long create(Theme theme) {
        String sql = "INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnail());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<Theme> findById(Long themeId) {
        try {
            String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?";
            Theme theme = jdbcTemplate.queryForObject(sql, themeRowMapper, themeId);
            return Optional.of(theme);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail FROM theme";
        return jdbcTemplate.query(sql, themeRowMapper
        );
    }

    @Override
    public void deleteById(Long themeId) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", themeId);
    }

    @Override
    public List<Theme> findRankByDate(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
                    SELECT t.id, t.name, t.description, t.thumbnail
                    FROM theme t
                    INNER JOIN reservation r ON t.id = r.theme_id
                    WHERE r.date BETWEEN ? AND ?
                    GROUP BY t.id
                    ORDER BY COUNT(r.id) DESC, t.name ASC
                    LIMIT ?;
                """;
        return jdbcTemplate.query(sql, themeRowMapper, startDate, endDate, limit);
    }
}
