package roomescape.repository;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Theme;

@Repository
public class JdbcThemeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Theme> rowMapper;

    public JdbcThemeRepository(DataSource dataSource, RowMapper<Theme> rowMapper) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = rowMapper;
    }

    public List<Theme> findAllThemes() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Theme insertTheme(Theme theme) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail", theme.getThumbnail());

        long savedId = jdbcInsert.executeAndReturnKey(parameterSource).longValue();
        return findThemeById(savedId);
    }

    public void deleteThemeById(long id) {
        String sql = "DELETE FROM theme WHERE id = :id";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(sql, parameterSource);
    }

    public boolean isThemeExistsById(long themeId) {
        String sql = "SELECT 1 FROM theme WHERE id = :themeId";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("themeId", themeId);
        List<Integer> result = jdbcTemplate.queryForList(sql, parameterSource, Integer.class);
        return !result.isEmpty();
    }

    public List<Theme> findTopThemesDescendingByReservationCount(String startDate, String endDate,
                                                                 int themeCount) {
        String sql = """
                    SELECT th.id, th.name, th.description, th.thumbnail
                    FROM theme AS th
                    INNER JOIN reservation AS r ON th.id = r.theme_id
                    WHERE r.date BETWEEN :startDate AND :endDate
                    GROUP BY th.id
                    ORDER BY COUNT(th.id) DESC
                    LIMIT :themeCount;
                """;

        SqlParameterSource paramMap = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("themeCount", themeCount);
        return jdbcTemplate.query(sql, paramMap, rowMapper);
    }

    private Theme findThemeById(long savedId) {
        String sql = """
                SELECT * 
                FROM theme AS t 
                WHERE t.id = :savedId;
                """;
        SqlParameterSource paramMap = new MapSqlParameterSource().addValue("savedId", savedId);
        return jdbcTemplate.query(sql, paramMap, rowMapper).get(0);
    }
}
