package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.AvailableTime;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

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

    private final RowMapper<AvailableTime> availableReservationTimeRowMapper =
            (rs, rowNum) -> new AvailableTime(
                    new ReservationTime(rs.getLong("id"), rs.getTime("start_at").toLocalTime()),
                    rs.getBoolean("available")
            );

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Optional<Theme> findById(long id) {
        return jdbcTemplate.query(
                "SELECT id, name, description, thumbnail_url FROM theme WHERE id = ?",
                themeRowMapper, id
        ).stream().findFirst();
    }

    public List<Theme> findAll() {
        return jdbcTemplate.query("SELECT id, name, description, thumbnail_url FROM theme", themeRowMapper);
    }

    public List<Theme> findPopularThemes(int size, LocalDate from, LocalDate to) {
        String sql = """
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

    public List<AvailableTime> findAvailableTimeById(long themeId, LocalDate date) {
        String sql = """
                SELECT
                      rt.id,
                      rt.start_at,
                      CASE WHEN r.id IS NULL THEN TRUE ELSE FALSE END AS available
                  FROM reservation_time rt
                  LEFT JOIN reservation r
                      ON rt.id = r.time_id
                       AND r.theme_id = ?
                       AND r.date = ?
                       AND r.status = 'CONFIRMED'
                """;
        return jdbcTemplate.query(sql, availableReservationTimeRowMapper, themeId, date);
    }

    public long save(String name, String description, String thumbnailUrl) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "name", name,
                "description", description,
                "thumbnail_url", thumbnailUrl
        )).longValue();
    }


    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }
}
