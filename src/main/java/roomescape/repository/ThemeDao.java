package roomescape.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.dto.ThemeRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ThemeDao {
    private static final RowMapper<Theme> rowMapper =
            (resultSet, rowNum) -> new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;


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

    public Optional<Theme> findById(final Long id) {
        String sql = "select id, name, description, thumbnail from theme where id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Theme> findPopularThemeByDate(final LocalDate fromDate, final LocalDate toDate, final Long count) {
        String sql = """
                 select theme.id, theme.name, theme.description, theme.thumbnail
                 from theme
                 left join reservation on reservation.theme_id = theme.id
                 and reservation.date between ? and ?
                 group by theme.id
                 order by count(theme_id) desc limit ?;
                """;
        return jdbcTemplate.query(sql, rowMapper, fromDate, toDate, count);
    }
}
