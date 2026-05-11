package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.ThemeRequest;

import java.sql.PreparedStatement;

@Repository
public class ThemeUpdatingDao {

    private final JdbcTemplate jdbcTemplate;

    public ThemeUpdatingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insert(ThemeRequest themeRequest) {
        String sql = "INSERT INTO theme (name, description, url) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, themeRequest.name());
            ps.setString(2, themeRequest.description());
            ps.setString(3, themeRequest.url());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void delete(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
