package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemePopularFilter;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ThemeJdbcDao implements ThemeDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Theme> rowMapper = (resultSet, rowNumber) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail"));

    public ThemeJdbcDao(final DataSource source) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(source);
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(final Theme theme) {
        final SqlParameterSource params = new BeanPropertySqlParameterSource(theme);
        final Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Theme(Objects.requireNonNull(id), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public List<Theme> findAll() {
        final String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        final String sql = "SELECT * FROM theme WHERE id = :id";
        final SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, parameterSource, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "DELETE FROM theme WHERE id = :id";
        final SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public List<Theme> findPopularThemesBy(final ThemePopularFilter filter) {
        final String sql = """
                SELECT th.id AS id, th.name AS name, th.description AS description, th.thumbnail AS thumbnail
                FROM theme th
                LEFT OUTER JOIN reservation r
                ON r.theme_id = th.id  AND r.date BETWEEN :startDate AND :endDate
                GROUP BY th.id
                ORDER BY COUNT(r.id) DESC, th.id
                LIMIT :limit
                """;
        final SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("startDate", filter.getStartDate())
                .addValue("endDate", filter.getEndDate())
                .addValue("limit", filter.getLimit());
        return jdbcTemplate.query(sql, parameterSource, rowMapper);
    }
}
