package roomescape.theme.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.Theme;

@Repository
public class ThemeDao {
    private static final RowMapper<Theme> rowMapper =
            (rs, rowNum) ->
                    new Theme(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("image"));

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public List<Theme> selectAll() {
        String sql = "SELECT * FROM theme";

        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Theme> selectById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = ?";

        List<Theme> themes = jdbcTemplate.query(sql, rowMapper, id);
        return themes.stream().findFirst();
    }

    public List<Theme> selectTopThemesByReservation(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.image,
                    COUNT(r.id) AS reservation_count
                FROM theme t
                JOIN reservation r ON r.theme_id = t.id
                WHERE r.date >= ?
                    AND r.date < ?
                GROUP BY t.id, t.name, t.description, t.image
                ORDER BY reservation_count DESC
                LIMIT ?;
                """;

        return jdbcTemplate.query(sql, rowMapper, startDate, endDate, limit);
    }

    public Theme insert(Theme theme) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("image", theme.getImage());
        long id = (long) simpleJdbcInsert.executeAndReturnKey(params);
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getImage());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM theme where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
