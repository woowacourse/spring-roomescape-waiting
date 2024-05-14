package roomescape.theme.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ThemeDao {

    private static final RowMapper<Theme> ROW_MAPPER = (resultSet, rowNum) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeDao(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme findById(final Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<Theme> findByStartDateAndEndDateWithLimit(final LocalDate startDate, final LocalDate endDate, final int limit) {
        String sql = """
                SELECT t.*
                FROM theme t
                RIGHT JOIN reservation r ON t.id = r.theme_id
                WHERE r.date BETWEEN ? AND ?
                GROUP BY r.theme_id
                ORDER BY COUNT(r.theme_id) DESC, t.id ASC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER, startDate, endDate, limit);
    }

    public Theme insert(final Theme theme) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail", theme.getThumbnail());
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Theme(id, theme);
    }

    public int deleteById(final Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        int deleteCount = jdbcTemplate.update(sql, id);

        return deleteCount;
    }
}
