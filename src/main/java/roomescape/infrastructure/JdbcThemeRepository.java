package roomescape.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) ->
            new Theme(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail")
            );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> parameter = Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail", theme.getThumbnail()
        );
        long id = simpleJdbcInsert.executeAndReturnKey(parameter).longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public List<Theme> findAll() {
        String sql = "select * from theme";
        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    @Override
    public int deleteById(long id) {
        String sql = "delete from theme where id = :id";
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("id", id);
        return jdbcTemplate.update(sql, parameter);
    }

    @Override
    public Optional<Theme> findByName(String name) {
        String sql = "select name from theme where name = :name";
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("name", name);
        try {
            Theme theme = this.jdbcTemplate.queryForObject(sql, parameter, THEME_ROW_MAPPER);
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "select * from theme where id = :id";
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("id", id);
        try {
            Theme theme = this.jdbcTemplate.queryForObject(sql, parameter, THEME_ROW_MAPPER);
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findByDateRangeOrderByReservationCountLimitN(LocalDate startDate,
                                                                    LocalDate endDate, int count) {
        String sql = """
                SELECT *
                FROM (SELECT theme_id, COUNT(*) AS count
                FROM reservation
                WHERE date BETWEEN :startDate AND :endDate
                GROUP BY theme_id
                ) AS r
                RIGHT JOIN theme AS t ON r.theme_id = t.id
                ORDER BY count DESC, name
                LIMIT :count
                """;
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("count", count);
        return jdbcTemplate.query(sql, parameter, THEME_ROW_MAPPER);
    }
}
