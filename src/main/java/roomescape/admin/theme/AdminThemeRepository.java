package roomescape.admin.theme;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;

@Repository
public class AdminThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public AdminThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> Theme.of(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("image_url"),
            resultSet.getLong("price")
    );

    public Theme save(Theme theme) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("image_url", theme.getImageUrl())
                .addValue("price", theme.getPrice());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Theme.of(id, theme.getName(), theme.getDescription(), theme.getImageUrl(), theme.getPrice());
    }

    public void deleteById(Long id) {
        String query = "delete from theme where id = ?";
        jdbcTemplate.update(query, id);
    }

    public List<Theme> findAll() {
        String query = "select * from theme";
        return jdbcTemplate.query(query, rowMapper);
    }

    public boolean existsById(Long id) {
        String query = "select count(*) from theme where id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    public boolean existsByName(String name) {
        String query = "select count(*) from theme where name = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, name);
        return count != null && count > 0;
    }
}
