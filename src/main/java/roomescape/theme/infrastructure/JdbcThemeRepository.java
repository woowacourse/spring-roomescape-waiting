package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcThemeRepository {

    private static RowMapper<Theme> ROW_MAPPER = (resultSet, rowNum) -> Theme.createWithId(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail")
    );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(final NamedParameterJdbcTemplate namedParameterJdbcTemplate, final DataSource dataSource) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(final Theme theme) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", theme.getName());
        params.put("description", theme.getDescription());
        params.put("thumbnail", theme.getThumbnail());
        return simpleJdbcInsert.executeAndReturnKey(params).longValue();
    }

    public List<Theme> findPopularThemes(final LocalDate start, final LocalDate end, final int popularCount) {
        String sql = """
                SELECT
                    count(*) as count,
                    t.id as id,
                    t.name as name,
                    t.description as description,
                    t.thumbnail as thumbnail
                FROM theme as t
                LEFT JOIN reservation as r ON t.id = r.theme_id
                WHERE r.date >= :start_date AND r.date <= :end_date
                GROUP BY id, name, description, thumbnail
                ORDER BY count DESC
                LIMIT :limit
                """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("start_date", start)
                .addValue("end_date", end)
                .addValue("limit", popularCount);

        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";

        return namedParameterJdbcTemplate.query(sql, ROW_MAPPER);
    }

    public boolean deleteById(final Long id) {
        String sql = "DELETE FROM theme where id = :id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", id);

        int count = namedParameterJdbcTemplate.update(sql, param);
        return count != 0;
    }

    public Theme findById(final Long id) {
        String sql = "SELECT * FROM theme WHERE id = :id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", id);

        return namedParameterJdbcTemplate.queryForObject(sql, param, ROW_MAPPER);
    }
}
