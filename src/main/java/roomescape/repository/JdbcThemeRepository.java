package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> THEME_MAPPER = (resultSet, row) ->
            new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(theme);
        Number id = jdbcInsert.executeAndReturnKey(parameterSource);
        return new Theme(id.longValue(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public List<Theme> findAllThemes() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, THEME_MAPPER);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, THEME_MAPPER, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findTopReservedThemesByDateRangeAndLimit(LocalDate start, LocalDate end, int limit) {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail, COUNT(*) as reservation_count
                FROM reservation as r
                INNER JOIN theme as t on r.theme_id = t.id
                WHERE r.reservation_date BETWEEN ? AND ?
                GROUP BY t.id
                ORDER BY reservation_count desc
                LIMIT(?)
                """;

        return jdbcTemplate.query(sql, THEME_MAPPER, start.toString(), end.toString(), limit);
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        try {
            jdbcTemplate.update(sql, id);
        } catch (DataAccessException exception) {
            throw new CustomException(ExceptionCode.NOT_FOUND_THEME);
        }
    }
}
