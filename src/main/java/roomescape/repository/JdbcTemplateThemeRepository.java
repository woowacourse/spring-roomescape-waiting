package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.repository.rowmapper.ThemeRowMapper;

@Repository
public class JdbcTemplateThemeRepository implements ThemeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ThemeRowMapper themeRowMapper;

    public JdbcTemplateThemeRepository(JdbcTemplate jdbcTemplate, ThemeRowMapper themeRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeRowMapper = themeRowMapper;
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail FROM theme";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public List<Theme> findAndOrderByPopularity(LocalDate start, LocalDate end, int count) {
        String sql = """
                SELECT
                    th.id AS id, th.name AS name, description, thumbnail, COUNT(*) AS count
                FROM theme th
                JOIN reservation r
                    ON r.theme_id = th.id
                WHERE
                    PARSEDATETIME(r.date,'yyyy-MM-dd') >= PARSEDATETIME(?,'yyyy-MM-dd')
                AND
                    PARSEDATETIME(r.date,'yyyy-MM-dd') <= PARSEDATETIME(?,'yyyy-MM-dd')
                GROUP BY th.id
                ORDER BY count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, themeRowMapper, start, end, count);
    }

    @Override
    public Optional<Theme> findById(long id) {
        String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?";
        return jdbcTemplate.query(sql, themeRowMapper, id)
                .stream()
                .findAny();
    }

    @Override
    public Theme save(Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        save(theme, keyHolder);
        long id = keyHolder.getKey().longValue();
        return new Theme(id, theme);
    }

    private void save(Theme theme, KeyHolder keyHolder) {
        jdbcTemplate.update(con -> {
            String sql = "INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"id"});
            pstmt.setString(1, theme.getName());
            pstmt.setString(2, theme.getDescription());
            pstmt.setString(3, theme.getThumbnail());
            return pstmt;
        }, keyHolder);
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
