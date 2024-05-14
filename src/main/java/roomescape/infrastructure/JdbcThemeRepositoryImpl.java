package roomescape.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

@Repository
public class JdbcThemeRepositoryImpl implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(Objects.requireNonNull(jdbcTemplate.getDataSource()))
            .withTableName("theme")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> saveSource = Map.ofEntries(
            Map.entry("name", theme.getName()),
            Map.entry("description", theme.getDescription()),
            Map.entry("thumbnail", theme.getThumbnail())
        );

        long id = simpleJdbcInsert.executeAndReturnKey(saveSource)
            .longValue();

        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM theme WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";

        return jdbcTemplate.query(sql, getThemeRowMapper());
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, getThemeRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isNameExists(String name) {
        String sql = "SELECT EXISTS(SELECT id FROM theme WHERE name = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, name);
    }

    @Override
    public List<Theme> findMostReservedThemesInPeriod(int period, int count) {
        String sql = """
            SELECT
                th.id AS id, 
                th.name AS name, 
                th.description AS description,
                th.thumbnail AS thumbnail, 
                COUNT(r.theme_id) AS count
            FROM theme AS th
            LEFT JOIN reservation AS r ON th.id = r.theme_id AND r.date BETWEEN TIMESTAMPADD(DAY, -?, NOW()) AND TIMESTAMPADD(DAY, -1, NOW())
            GROUP BY th.id
            ORDER BY count DESC
            LIMIT ?
            """;
        return jdbcTemplate.query(sql, getThemeRowMapper(), period + 1, count);
    }

    private RowMapper<Theme> getThemeRowMapper() {
        return (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail")
        );
    }
}
