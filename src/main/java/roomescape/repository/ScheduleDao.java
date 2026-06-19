package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;

@Repository
public class ScheduleDao {

    private static final RowMapper<Schedule> scheduleRowMapper = (rs, rowNum) -> new Schedule(
            rs.getLong("id"),
            new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail"),
                    rs.getInt("theme_price")
            ),
            rs.getDate("date").toLocalDate(),
            new ReservationTime(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime())
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

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

    public Optional<Schedule> findById(Long id) {
        String sql = """
            SELECT s.id,
                   s.date,
                   t.id   AS time_id,
                   t.start_at AS time_value,
                   th.id  AS theme_id,
                   th.name AS theme_name,
                   th.description AS theme_description,
                   th.thumbnail_url AS theme_thumbnail,
                   th.price AS theme_price
            FROM schedule AS s
            INNER JOIN reservation_time AS t  ON s.time_id  = t.id
            INNER JOIN theme            AS th ON s.theme_id = th.id
            WHERE s.id = ?
            """;
        List<Schedule> results = jdbcTemplate.query(sql, scheduleRowMapper, id);
        return results.stream().findFirst();
    }

    public boolean lockById(Long id) {
        String sql = """
            SELECT id
            FROM schedule
            WHERE id = ?
            FOR UPDATE
            """;

        List<Long> results = jdbcTemplate.queryForList(sql, Long.class, id);
        return !results.isEmpty();
    }

    public Optional<Schedule> findByDateAndTimeIdAndThemeId(
            LocalDate date,
            Long timeId,
            Long themeId
    ) {
        String sql = """
            SELECT s.id,
                   s.date,
                   t.id AS time_id,
                   t.start_at AS time_value,
                   th.id AS theme_id,
                   th.name AS theme_name,
                   th.description AS theme_description,
                   th.thumbnail_url AS theme_thumbnail,
                   th.price AS theme_price
            FROM schedule AS s
            INNER JOIN reservation_time AS t
                    ON s.time_id = t.id
            INNER JOIN theme AS th
                    ON s.theme_id = th.id
            WHERE s.date = ?
              AND s.time_id = ?
              AND s.theme_id = ?
            """;

        List<Schedule> result = jdbcTemplate.query(
                sql,
                scheduleRowMapper,
                date,
                timeId,
                themeId
        );

        return result.stream().findFirst();
    }

    public boolean existsByTimeId(Long timeId) {
        String sql = "select exists (select 1 from schedule where time_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
    }

    public boolean existsByThemeId(Long themeId) {
        String sql = "select exists (select 1 from schedule where theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
    }
}
