package roomescape.theme.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

import java.util.List;
import java.util.Optional;

@Repository
public class ThemeDao implements ThemeRepository {

    private static final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(theme);
        Long id = simpleJdbcInsert.executeAndReturnKey(sqlParameterSource).longValue();
        return new Theme(id, theme);
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        try {
            String sql = "SELECT * FROM theme WHERE id = ?";
            Theme theme = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Boolean existsByName(String name) {
        String sql = "SELECT EXISTS (SELECT id FROM theme WHERE name = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, name);
    }
}

