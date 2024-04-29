package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Theme> themeMapper;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        this.simpleJdbcInsert = new SimpleJdbcInsert(Objects.requireNonNull(jdbcTemplate.getDataSource()))
            .withTableName("theme")
            .usingGeneratedKeyColumns("id");

        this.themeMapper = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail")
        );
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> saveSource = Map.ofEntries(
            Map.entry("name", theme.getName()),
            Map.entry("description", theme.getDescription()),
            Map.entry("thumbnail", theme.getThumbnail())
        );

        long id = simpleJdbcInsert.executeAndReturnKey(saveSource).longValue();
        return findById(id);
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, themeMapper);
    }

    @Override
    public Theme findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, themeMapper, id);
    }

    @Override
    public Boolean isExists(String name) {
        String sql = "SELECT EXISTS(SELECT id FROM theme WHERE name = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, name);
    }

    @Override
    public List<Theme> findPopular(int start, int end, int count) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(start);
        LocalDate endDate = today.minusDays(end);

        String sql = """
            SELECT
                th.id AS id,
                th.name AS name,
                th.description AS description,
                th.thumbnail AS thumbnail,
                COUNT(r.theme_id) AS count
            FROM theme AS th
            LEFT JOIN reservation AS r ON th.id = r.theme_id AND r.date BETWEEN ? AND ?
            GROUP BY th.id
            ORDER BY count DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, themeMapper, startDate, endDate, count);
    }
}
