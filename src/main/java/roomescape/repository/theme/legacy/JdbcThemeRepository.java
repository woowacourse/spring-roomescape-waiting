package roomescape.repository.theme.legacy;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.domain.theme.Theme;
import roomescape.repository.theme.ThemeRepository;

public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Theme> themeRowMapper = (resultSet, rowNumber) -> {
        long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        String thumbnail = resultSet.getString("thumbnail");

        return new Theme(id, name, description, thumbnail);
    };

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long save(Theme theme) {
        String sql = "insert into theme (name,description,thumbnail) values(?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnail());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public List<Theme> findAll() {
        String sql = "select id, name, description, thumbnail from theme";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(long id) {
        try {
            String sql = "select id, name, description, thumbnail from theme where id=?";
            return Optional.of(jdbcTemplate.queryForObject(sql, themeRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "delete from theme where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
