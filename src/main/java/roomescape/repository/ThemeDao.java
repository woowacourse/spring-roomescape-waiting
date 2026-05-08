package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.dto.AvailableReservationTimeResponse;

@Repository
public class ThemeDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Theme> themeRowMapper = (rs, rowNum) -> new Theme(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_url")
    );

    private final RowMapper<AvailableReservationTimeResponse> availableReservationTimeRowMapper =
            (rs, rowNum) -> new AvailableReservationTimeResponse(
                    rs.getLong("id"),
                    rs.getObject("start_at", LocalTime.class),
                    rs.getBoolean("available")
            );

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(String name, String description, String thumbnailUrl) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "name", name,
                "description", description,
                "thumbnail_url", thumbnailUrl
        )).longValue();
    }

    public List<Theme> findPopularThemes(int size, LocalDate from, LocalDate to) {
            final String sql = """
                            SELECT
                                th.id,
                                th.name,
                                th.description,
                                th.thumbnail_url
                            FROM reservation AS r
                            INNER JOIN theme AS th ON r.theme_id = th.id
                            WHERE r.date BETWEEN ? AND ?
                            GROUP BY
                                th.id,
                                th.name,
                                th.description,
                                th.thumbnail_url
                            ORDER BY COUNT(r.id) DESC
                            LIMIT ?
                            """;
            return jdbcTemplate.query(sql, themeRowMapper, from, to, size);
    }


    public List<AvailableReservationTimeResponse> findAvailableTimeById(long themeId, String date) {
            final String sql = """
                    SELECT                                                                                                                                                                                                         \s
                          rt.id,                                                                                                                                                                                                     \s
                          rt.start_at,                                                                                                                                                                                               \s
                          CASE WHEN r.id IS NULL THEN TRUE ELSE FALSE END AS available                                                                                                                                               \s
                      FROM reservation_time rt                                                                                                                                                                                       \s
                      LEFT JOIN reservation r                                                                                                                                                                                      \s
                          ON rt.id = r.time_id
                           AND r.theme_id = ?
                           AND r.date = ?
                    """;
            return jdbcTemplate.query(sql, availableReservationTimeRowMapper, themeId, date);
    }

    public List<Theme> findAll() {
        return jdbcTemplate.query( "SELECT id, name, description, thumbnail_url FROM theme", themeRowMapper);
    }


    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
