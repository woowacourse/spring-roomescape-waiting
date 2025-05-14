package roomescape.theme.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public class ThemeDaoImpl implements ThemeDao {
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ThemeDaoImpl(DataSource dataSource) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail FROM theme";
        return namedParameterJdbcTemplate.query(sql,
                (resultSet, rowNum) -> createTheme(resultSet));
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        String sql = "SELECT id, name, description, thumbnail from theme where id = :id";
        Map<String, Object> parameter = Map.of("id", id);

        try {
            return Optional.of(namedParameterJdbcTemplate.queryForObject(sql, parameter,
                    (resultSet, rowNum) -> createTheme(resultSet)));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findRankedByPeriod(final LocalDate startDate, final LocalDate endDate) {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail 
                FROM theme AS t 
                INNER JOIN reservation AS r ON r.theme_id = t.id 
                WHERE r.date BETWEEN :startDate AND :endDate 
                GROUP BY t.id, t.name, t.description, t.thumbnail 
                ORDER BY COUNT(r.id) DESC 
                LIMIT 10
                """;
        Map<String, Object> parameters = Map.of("startDate", startDate, "endDate", endDate);

        return namedParameterJdbcTemplate.query(sql, parameters,
                (resultSet, rowNum) -> createTheme(resultSet));
    }

    @Override
    public Boolean existsByName(final String name) {
        String sql = "SELECT COUNT(*) FROM theme WHERE name = :name";
        Map<String, Object> parameters = Map.of("name", name);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
        return count != 0;
    }

    @Override
    public Boolean existsByReservationThemeId(final Long themeId) {
        String sql = """
                SELECT COUNT(*) FROM theme AS e 
                INNER JOIN reservation AS r 
                ON e.id = r.theme_id
                WHERE r.theme_id = :themeId
                """;
        Map<String, Object> parameter = Map.of("themeId", themeId);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameter, Integer.class);
        return count != 0;
    }
    
    @Override
    public Theme add(final Theme theme) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", theme.getName());
        parameters.put("description", theme.getDescription());
        parameters.put("thumbnail", theme.getThumbnail());
        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public void deleteById(final Long id) {
        String sql = "DELETE FROM theme WHERE id = :id";
        Map<String, Object> parameter = Map.of("id", id);
        namedParameterJdbcTemplate.update(sql, parameter);
    }

    private Theme createTheme(final ResultSet resultSet) throws SQLException {
        return new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );
    }
}
