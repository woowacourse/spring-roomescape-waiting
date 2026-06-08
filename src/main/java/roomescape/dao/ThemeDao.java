package roomescape.dao;

import static roomescape.dao.rowMapper.ReservationTimeMapper.RESERVATION_TIME_STATUS_ROW_MAPPER;
import static roomescape.dao.rowMapper.ThemeMapper.THEME_ROW_MAPPER;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.dto.TimeQueryResult;
import roomescape.domain.reservation.theme.Theme;

@Repository
public class ThemeDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ThemeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Optional<Theme> findThemeById(Long id) {
        String sql = """
                SELECT id, name, description, url 
                FROM theme 
                WHERE id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        THEME_ROW_MAPPER,
                        id
                ).stream()
                .findFirst();
    }

    public List<Theme> findAllThemes() {
        return jdbcTemplate.query(
                "SELECT id, name, description, url FROM theme",
                THEME_ROW_MAPPER
        );
    }

    public List<Theme> findTopThemes(Long count, LocalDate today) {
        return jdbcTemplate.query(
                """
                           SELECT
                           t.id, t.name, t.description, t.url, r.reservation_count
                           FROM theme t
                           INNER JOIN (
                               SELECT theme_id, COUNT(id) AS reservation_count
                               FROM reservation
                               WHERE date >= ? AND date < ?
                               GROUP BY theme_id
                           ) r ON t.id = r.theme_id
                           ORDER BY r.reservation_count DESC
                           LIMIT ?;
                        """,
                THEME_ROW_MAPPER,
                today.minusDays(7),
                today,
                count
        );
    }


    public Theme save(Theme theme) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", theme.getName().value());
        params.put("description", theme.getDescription().value());
        params.put("url", theme.getUrl().value());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getUrl()
        );
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }

    public boolean existsByName(String name) {
        Boolean result = jdbcTemplate.queryForObject("""
                        SELECT EXISTS(
                            SELECT 1
                            FROM theme
                            WHERE name = ?
                        )
                        """,
                Boolean.class,
                name
        );
        return Boolean.TRUE.equals(result);
    }

    public List<TimeQueryResult> findTimeStatusBy(Long id, LocalDate date) {
        return jdbcTemplate.query(
                """
                          SELECT t.id AS time_id,
                                 t.start_at,
                                 CASE
                                     WHEN EXISTS (
                                         SELECT 1 FROM reservation r
                                         WHERE r.time_id = t.id AND r.theme_id = ? AND r.date = ?
                                     )
                                     OR EXISTS (
                                         SELECT 1 FROM waiting w
                                         WHERE w.time_id = t.id AND w.theme_id = ? AND w.date = ?
                                     )
                                     THEN FALSE ELSE TRUE
                                 END AS reservable
                          FROM reservation_time t
                          ORDER BY t.start_at
                        """,
                RESERVATION_TIME_STATUS_ROW_MAPPER,
                id,
                date,
                id,
                date
        );
    }
}
