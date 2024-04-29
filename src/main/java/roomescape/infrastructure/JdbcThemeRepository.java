package roomescape.infrastructure;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcThemeRepository implements ThemeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) -> new Theme(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail")
    );

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";

        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";

        try {
            Theme theme = jdbcTemplate.queryForObject(sql, themeRowMapper, id);
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findMostReservedThemesWithinDays(int day, int limit) {
        String sql = """
                SELECT 
                    th.id AS theme_id, 
                    th.name AS theme_name, 
                    th.description AS theme_description, 
                    th.thumbnail AS theme_thumbnail, 
                    COUNT(r.id) AS total_reservations 
                FROM theme AS th 
                INNER JOIN reservation AS r ON r.theme_id = th.id 
                WHERE r.date > ? 
                GROUP BY th.id 
                ORDER BY total_reservations DESC 
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, themeRowMapper, LocalDate.now().minusDays(day), limit);
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, String> params = Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail", theme.getThumbnail()
        );
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }
}
