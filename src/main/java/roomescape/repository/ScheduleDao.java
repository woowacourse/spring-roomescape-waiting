package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.Time;

@Repository
public class ScheduleDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Schedule> scheduleRowMapper = (rs, rowNum) -> new Schedule(
            rs.getLong("id"),
            rs.getDate("date").toLocalDate(),
            new Time(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
            new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"), rs.getString("theme_thumbnail"))
    );

    public ScheduleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("schedule")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(LocalDate date, Long timeId, Long themeId) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "date", date,
                "time_id", timeId,
                "theme_id", themeId
        )).longValue();
    }

    public Schedule findById(Long id) {
        String sql = """
                SELECT s.id,
                       s.date,
                       t.id   AS time_id,
                       t.start_at AS time_value,
                       th.id  AS theme_id,
                       th.name AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail_url AS theme_thumbnail
                FROM schedule AS s
                INNER JOIN reservation_time AS t  ON s.time_id  = t.id
                INNER JOIN theme            AS th ON s.theme_id = th.id
                WHERE s.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, scheduleRowMapper, id);
    }

    public Optional<Long> findIdByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT s.id
                FROM schedule s
                WHERE s.date     = ?
                  AND s.time_id  = ?
                  AND s.theme_id = ?
                """;
        List<Long> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id"),
                date, timeId, themeId
        );
        return result.stream().findFirst();
    }

    public List<Schedule> findAll() {
        String sql = """
                SELECT s.id,
                       s.date,
                       t.id   AS time_id,
                       t.start_at AS time_value,
                       th.id  AS theme_id,
                       th.name AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail_url AS theme_thumbnail
                FROM schedule AS s
                INNER JOIN reservation_time AS t  ON s.time_id  = t.id
                INNER JOIN theme            AS th ON s.theme_id = th.id
                """;
        return jdbcTemplate.query(sql, scheduleRowMapper);
    }
}
