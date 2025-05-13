package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;

@Repository
public class JdbcThemeDao implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert themeInserter;
    private final RowMapper<Theme> themeMapper = (resultSet, rowNum) ->
            new Theme(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );

    public JdbcThemeDao(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeInserter = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(final Theme theme) {
        final Map<String, Object> parameters = Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail", theme.getThumbnail()
        );
        long id = themeInserter.executeAndReturnKey(parameters).longValue();
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public List<Theme> findAll() {
        final String sql = "SELECT id, name, description, thumbnail FROM theme";
        return jdbcTemplate.query(sql, themeMapper);
    }

    @Override
    public Optional<Theme> findById(final long id) {
        final String sql = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?";
        return jdbcTemplate.query(sql, themeMapper, id).stream().findFirst();
    }

    @Override
    public List<Theme> findPopularThemes(final LocalDate from, final LocalDate to, final int count) {
        final String sql = """
                     SELECT t.id, t.name, t.description, t.thumbnail
                     FROM theme AS t
                     INNER JOIN reservation AS r ON t.id = r.theme_id
                     WHERE r.date >= ? AND r.date <= ?
                     GROUP BY t.id
                     ORDER BY COUNT(*) DESC
                     LIMIT ?
                """;
        return jdbcTemplate.query(sql, themeMapper, from, to, count);
    }

    @Override
    public void deleteById(final long id) {
        final String sql = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByName(final ThemeName name) {
        final String sql = "SELECT 1 FROM theme WHERE name = ? LIMIT 1";
        final List<Integer> result = jdbcTemplate.query(sql, (resultSet, rowNumber) -> resultSet.getInt(1),
                name.getName());
        return !result.isEmpty();
    }
}
