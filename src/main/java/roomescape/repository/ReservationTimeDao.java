package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.AvailableTimeResult;

@Repository
public class ReservationTimeDao {

    private static final RowMapper<ReservationTime> timeRowMapper = (rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime()
    );

    private static final RowMapper<AvailableTimeResult> timeResultRowMapper = (rs, rowNum) -> new AvailableTimeResult(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime(),
            rs.getInt("reservation_count")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(LocalTime startAt) {
        return jdbcInsert.executeAndReturnKey(Map.of("start_at", startAt)).longValue();
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }

    public List<ReservationTime> findAll() {
        return jdbcTemplate.query("SELECT id, start_at FROM reservation_time ORDER BY start_at", timeRowMapper);
    }

    public List<AvailableTimeResult> findAvailableTimes(long themeId, LocalDate date, ReservationStatus inActiveStatus) {
        final String sql = """
            SELECT
                rt.id,
                rt.start_at,
                COUNT(r.id) AS reservation_count
            FROM reservation_time rt
            LEFT JOIN schedule s ON s.time_id = rt.id
                AND s.theme_id = ?
                AND s.date = ?
            LEFT JOIN reservation r ON r.schedule_id = s.id
                AND r.status != ?
            GROUP BY rt.id, rt.start_at
            ORDER BY rt.start_at
            """;

        return jdbcTemplate.query(
                sql,
                timeResultRowMapper,
                themeId,
                date,
                inActiveStatus.name()
        );
    }

    public boolean existsByStartAt(LocalTime startAt) {
        String sql = "select exists (select 1 from reservation_time where start_at = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, startAt);
    }
}
