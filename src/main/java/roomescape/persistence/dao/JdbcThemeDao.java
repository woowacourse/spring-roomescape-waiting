package roomescape.persistence.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.business.domain.Theme;

@Repository
public class JdbcThemeDao implements ThemeDao {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String THUMBNAIL = "thumbnail";
    private static final RowMapper<Theme> themeRowMapper =
            (rs, rowNum) -> new Theme(
                    rs.getLong(ID),
                    rs.getString(NAME),
                    rs.getString(DESCRIPTION),
                    rs.getString(THUMBNAIL)
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns(ID);
    }

    @Override
    public Theme insert(final Theme theme) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(NAME, theme.getName());
        parameters.put(DESCRIPTION, theme.getDescription());
        parameters.put(THUMBNAIL, theme.getThumbnail());
        final Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        final String sql = """
                SELECT id, name, description, thumbnail 
                FROM theme 
                WHERE id = ?
                """;
        try {
            final Theme theme = jdbcTemplate.queryForObject(sql, themeRowMapper, id);
            return Optional.of(theme);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findAll() {
        final String sql = """
                SELECT id, name, description, thumbnail 
                FROM theme
                """;
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public boolean deleteById(final Long id) {
        final String sql = """
                DELETE FROM theme 
                WHERE id = ?
                """;
        final int updatedRowCount = jdbcTemplate.update(sql, id);
        return updatedRowCount >= 1;
    }

    @Override
    public boolean existsById(final Long themeId) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1 
                    FROM theme 
                    WHERE id = ?
                ) AS is_exist
                """;
        final int flag = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return flag == 1;
    }

    @Override
    public boolean existsByName(final String name) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM theme
                    WHERE name = ?
                ) AS is_exist
                """;
        final int flag = jdbcTemplate.queryForObject(sql, Integer.class, name);
        return flag == 1;
    }
}
