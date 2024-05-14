package roomescape.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.theme.NotFoundThemeException;

@Repository
public class ThemeDao implements ThemeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail")
    );

    public ThemeDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        List<Theme> theme = jdbcTemplate.query(sql, themeRowMapper, id);
        return DataAccessUtils.optionalResult(theme);
    }

    @Override
    public List<Theme> findThemesByPeriodWithLimit(String startDate, String endDate, int limit) {
        String sql = """
                SELECT theme.id, theme.name, theme.description, theme.thumbnail
                FROM reservation
                LEFT JOIN theme ON theme.id=reservation.theme_id
                WHERE reservation.date >= ? AND reservation.date <= ?
                GROUP BY theme.id
                ORDER BY COUNT(*) DESC
                LIMIT ?;
                """;
        return jdbcTemplate.query(sql, themeRowMapper, startDate, endDate, limit);
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", theme.getName());
        parameters.put("description", theme.getDescription());
        parameters.put("thumbnail", theme.getThumbnail());
        Long id = (Long) jdbcInsert.executeAndReturnKey(parameters);
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public void delete(Theme theme) {
        String sql = "DELETE FROM theme WHERE id = ?";
        int update = jdbcTemplate.update(sql, theme.getId());
        checkRemoved(update);
    }

    private void checkRemoved(int count) {
        if (count < 1) {
            throw new NotFoundThemeException();
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM theme";
        jdbcTemplate.update(sql);
    }
}
