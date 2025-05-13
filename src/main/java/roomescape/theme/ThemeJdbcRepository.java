package roomescape.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class ThemeJdbcRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ThemeJdbcRepository(
            @Autowired final JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(final Theme theme) {
        final Map<String, String> themeParameter = Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail", theme.getThumbnail()
        );

        return simpleJdbcInsert
                .executeAndReturnKey(themeParameter)
                .longValue();
    }

    @Override
    public Theme findById(final Long id) {
        final String sql = "SELECT * FROM theme WHERE id=?";
        return jdbcTemplate.queryForObject(sql, getRowMapper(), id);
    }

    @Override
    public List<Theme> findAllOrderByRank(
            final LocalDate from,
            final LocalDate to,
            final int size
    ) {
        final String sql = """
                SELECT t.id AS id, 
                       t.name AS name, 
                       t.description AS description, 
                       t.thumbnail AS thumbnail  
                FROM theme AS t INNER JOIN reservation AS r  
                ON r.theme_id = t.id  
                WHERE r.date BETWEEN ? AND ?  
                GROUP BY t.id  
                ORDER BY count(*) DESC  
                LIMIT ? 
                """;

        return jdbcTemplate.query(sql, getRowMapper(), from, to, size);
    }

    @Override
    public List<Theme> findAll() {
        final String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, getRowMapper());
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "DELETE FROM theme WHERE id=?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Boolean existsById(final Long id) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM theme WHERE id=?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    private static RowMapper<Theme> getRowMapper() {
        return (resultSet, rowNum) ->
                new Theme(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("thumbnail"));
    }
}
