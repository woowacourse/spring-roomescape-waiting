package roomescape.theme.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.business.domain.Theme;
import roomescape.theme.business.repository.ThemeDao;

@Repository
public class JdbcThemeDao implements ThemeDao {

    private static final RowMapper<Theme> ROW_MAPPER = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcThemeDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Theme save(final Theme theme) {
        final String sql = "INSERT INTO theme (name, description, thumbnail) VALUES (:name, :description, :thumbnail)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        final MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail", theme.getThumbnail());
        jdbcTemplate.update(sql, mapSqlParameterSource, keyHolder);

        final Number key = keyHolder.getKey();
        return new Theme(key.longValue(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public List<Theme> findAll() {
        final String sql = "SELECT * FROM theme";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public int deleteById(final Long id) {
        final String sql = "DELETE FROM theme WHERE id = :id";
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public List<Theme> sortByRank() {
        final String sql = """
                SELECT
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail
                FROM
                    reservation r
                JOIN
                    theme t ON r.theme_id = t.id
                WHERE
                    r.date BETWEEN DATEADD('DAY', -7, CURRENT_DATE) AND DATEADD('DAY', -1, CURRENT_DATE)
                GROUP BY
                    t.id, t.name, t.description, t.thumbnail
                ORDER BY
                    COUNT(r.id) DESC
                LIMIT 10
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public boolean existByName(final String name) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM theme WHERE name = :name)";

        return Boolean.TRUE == jdbcTemplate.queryForObject(
                sql, new MapSqlParameterSource("name", name), Boolean.class);

    }

    @Override
    public Optional<Theme> findById(final Long id) {
        final String sql = "SELECT * FROM theme WHERE id = :id";

        final List<Theme> findTheme = jdbcTemplate.query(
                sql, new MapSqlParameterSource("id", id),
                ROW_MAPPER);
        return findTheme.stream().findFirst();
    }
}
