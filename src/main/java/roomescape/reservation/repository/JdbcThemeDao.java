package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

@Repository
public class JdbcThemeDao implements ThemeDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Theme> themeMapper = (resultSet, rowNum) ->
            new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    public JdbcThemeDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isExists(final ThemeName name) {
        final String sql = "SELECT COUNT(*) FROM theme WHERE name = :name";
        final SqlParameterSource parameters = new MapSqlParameterSource("name", name.getName());
        final long count = jdbcTemplate.queryForObject(sql, parameters, Long.class);
        return count > 0;
    }

    @Override
    public Theme save(final Theme theme) {
        final String sql = "INSERT INTO theme(name, description, thumbnail) VALUES(:name, :description, :thumbnail)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail", theme.getThumbnail());
        jdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        return new Theme(keyHolder.getKeyAs(Long.class), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public List<Theme> findAll() {
        final String sql = "SELECT id, name, description, thumbnail FROM theme";
        return jdbcTemplate.query(sql, themeMapper);
    }

    @Override
    public Optional<Theme> findById(final long id) {
        final String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = :id";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        try {
            Theme theme = jdbcTemplate.queryForObject(sql, parameters, themeMapper);
            return Optional.of(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(final long id) {
        final String sql = "DELETE FROM theme WHERE id = :id";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, parameters);
    }

    @Override
    public List<Theme> findPopularThemes(final LocalDate from, final LocalDate to, final int count) {
        final String sql = """
                     SELECT t.id, t.name, t.description, t.thumbnail
                     FROM theme AS t
                     INNER JOIN reservation AS r ON t.id = r.theme_id
                     WHERE r.date >= :fromDate AND r.date <= :toDate
                     GROUP BY t.id
                     ORDER BY COUNT(*) DESC
                     LIMIT :count
                """;
        final SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("fromDate", from)
                .addValue("toDate", to)
                .addValue("count", count);
        return jdbcTemplate.query(sql, parameters, themeMapper);
    }
}
