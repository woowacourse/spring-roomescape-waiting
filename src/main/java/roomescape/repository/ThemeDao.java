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
import roomescape.dto.AvailableTimeResponse;

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

    private final RowMapper<AvailableTimeResponse> availableReservationTimeRowMapper =
            (rs, rowNum) -> new AvailableTimeResponse(
                    rs.getLong("id"),
                    rs.getObject("start_at", LocalTime.class),
                    rs.getBoolean("available"),
                    rs.getInt("waiting_count")
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
                FROM schedule AS s
                INNER JOIN theme AS th ON s.theme_id = th.id
                WHERE s.date BETWEEN ? AND ?
                GROUP BY
                    th.id,
                    th.name,
                    th.description,
                    th.thumbnail_url
                ORDER BY COUNT(s.id) DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, themeRowMapper, from, to, size);
    }

    public List<AvailableTimeResponse> findAvailableTimeById(long themeId, String date) {
        final String sql = """
                SELECT
                    rt.id,
                    rt.start_at,
                    CASE WHEN COUNT(CASE WHEN r.status = 'RESERVED' THEN 1 END) = 0
                         THEN TRUE
                         ELSE FALSE
                    END AS available,
                    CASE WHEN COUNT(CASE WHEN r.status = 'RESERVED' THEN 1 END) > 1
                         THEN COUNT(CASE WHEN r.status = 'RESERVED' THEN 1 END) - 1
                         ELSE 0
                    END AS waiting_count
                FROM reservation_time rt
                LEFT JOIN schedule s
                    ON rt.id       = s.time_id
                    AND s.theme_id = ?
                    AND s.date     = ?
                LEFT JOIN reservation r
                    ON r.schedule_id = s.id
                GROUP BY rt.id, rt.start_at
                ORDER BY rt.start_at
                """;
        return jdbcTemplate.query(sql, availableReservationTimeRowMapper, themeId, date);
    }

    public List<Theme> findAll() {
        return jdbcTemplate.query("SELECT id, name, description, thumbnail_url FROM theme", themeRowMapper);
    }

    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
