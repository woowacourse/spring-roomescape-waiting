package roomescape.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.rowmapper.ThemeRowMapper;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

@Repository
public class ThemeDao implements ThemeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final ThemeRowMapper themeRowMapper;

    public ThemeDao(JdbcTemplate jdbcTemplate, DataSource dataSource, ThemeRowMapper themeRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.themeRowMapper = themeRowMapper;
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        List<Theme> theme = jdbcTemplate.query(sql, themeRowMapper, id);
        return DataAccessUtils.optionalResult(theme);
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", theme.getName());
        parameters.put("description", theme.getDescription());
        parameters.put("thumbnail", theme.getThumbnail());
        long id = jdbcInsert.executeAndReturnKey(parameters).longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public void delete(Theme theme) {
        String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, theme.getId());
    }
}
