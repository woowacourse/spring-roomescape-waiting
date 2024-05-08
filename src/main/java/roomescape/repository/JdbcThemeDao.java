package roomescape.repository;

import java.time.LocalDate;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.model.Theme;

@Repository
public class JdbcThemeDao implements ThemeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertActor;

    private static final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) ->
            new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    public JdbcThemeDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAllThemes() {
        String sql = "SELECT id, name, description, thumbnail FROM theme";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public Theme addTheme(Theme theme) {
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(theme);
        Number newId = insertActor.executeAndReturnKey(parameters);
        return new Theme(newId.longValue(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public void deleteTheme(long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Theme findThemeById(long id) {
        String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, themeRowMapper, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Theme> findThemeRankingByDate(LocalDate before, LocalDate after, int limit) {
        String sql = """
                SELECT th.id, th.name, th.description, th.thumbnail 
                FROM reservation AS r 
                INNER JOIN theme AS th on r.theme_id = th.id 
                WHERE r.date BETWEEN ? AND ? 
                GROUP BY r.theme_id 
                ORDER BY COUNT(r.theme_id) DESC
                limit ? 
                """;
        try {
            return jdbcTemplate.query(sql, themeRowMapper, before, after, limit);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }
}
