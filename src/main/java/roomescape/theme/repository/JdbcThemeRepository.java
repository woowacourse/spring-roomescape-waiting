package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> Theme.restore(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("image_url"),
            resultSet.getLong("price")
    );

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("image_url", theme.getImageUrl())
                .addValue("price", theme.getPrice());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Theme.restore(id, theme.getName(), theme.getDescription(), theme.getImageUrl(), theme.getPrice());
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String query = "SELECT * FROM theme WHERE id = ?";
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    @Override
    public List<Theme> findAll() {
        String query = "SELECT * FROM theme ORDER BY id ASC";
        return jdbcTemplate.query(query, rowMapper);
    }

    @Override
    public boolean existsReservationByThemeId(Long themeId) {
        String query = "SELECT COUNT(*) FROM reservation WHERE theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, themeId);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM theme WHERE id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public List<Long> findTopThemeIds(LocalDate startDate, LocalDate endDate, int limit) {
        String query = """
                SELECT r.theme_id
                FROM reservation r
                WHERE r.date BETWEEN ? AND ?
                GROUP BY r.theme_id
                ORDER BY COUNT(r.id) DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(query, (rs, rowNum) -> rs.getLong("theme_id"), startDate, endDate, limit);
    }

    @Override
    public List<Theme> findAllByIds(List<Long> ids) {
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        String query = "SELECT id, name, description, image_url, price FROM theme WHERE id IN (" + placeholders + ") ORDER BY id ASC";
        return jdbcTemplate.query(query, rowMapper, ids.toArray());
    }
}
