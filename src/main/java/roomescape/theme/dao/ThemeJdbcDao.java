package roomescape.theme.dao;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.theme.domain.Theme;

@Component
public class ThemeJdbcDao implements ThemeDao {

    public static final RowMapper<Theme> THEME_ROW_MAPPER = (resultSet, rowNum) -> Theme.themeOf(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail"));

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeJdbcDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(theme);
        long id = jdbcInsert.executeAndReturnKey(sqlParameterSource).longValue();
        theme.setIdOnSave(id);
        return theme;
    }

    @Override
    public Optional<Theme> findById(long themeId) {
        String findThemeSql = """
                SELECT id, name, description, thumbnail
                FROM theme
                WHERE theme.id = ?
                """;

        try {
            Theme theme = jdbcTemplate.queryForObject(findThemeSql, THEME_ROW_MAPPER, themeId);

            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findAllThemes() {
        String findAllThemesSql = "SELECT id, name, description, thumbnail FROM theme";

        return jdbcTemplate.query(findAllThemesSql, THEME_ROW_MAPPER);
    }

    @Override
    public void deleteById(long themeId) {
        String deleteThemeSql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(deleteThemeSql, themeId);
    }
}
