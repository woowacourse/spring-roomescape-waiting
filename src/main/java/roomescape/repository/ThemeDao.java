package roomescape.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;

@Repository
public class ThemeDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Theme> rowMapper =
            (resultSet, rowNum) -> new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    public ThemeDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingColumns("name", "description", "thumbnail")
                .usingGeneratedKeyColumns("id");
    }

    public List<Theme> findAll() {
        String sql = "select id, name, description, thumbnail from theme";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Long create(final ThemeRequest themeRequest) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("name", themeRequest.name())
                .addValue("description", themeRequest.description())
                .addValue("thumbnail", themeRequest.thumbnail());
        return jdbcInsert.executeAndReturnKey(parameterSource).longValue();
    }

    public void delete(final Long id) {
        String sql = "delete from theme where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Theme findById(final Long id) {
        String sql = "select id, name, description, thumbnail from theme where id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }
}
