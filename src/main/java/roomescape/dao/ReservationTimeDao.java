package roomescape.dao;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeStatus;

@Repository
@Transactional(readOnly = true)
public class ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time",
                (rs, rowNum) -> new ReservationTime(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime()
                )
        );
    }

    public boolean existsByTimeId(Long timeId) {
        Boolean result = jdbcTemplate.queryForObject("""
                        SELECT EXISTS(
                            SELECT *
                            FROM reservation_time rt
                            JOIN reservation r ON r.time_id = rt.id
                            WHERE rt.id = ?
                        )
                        """,
                Boolean.class,
                timeId
        );
        return Boolean.TRUE.equals(result);
    }

    public boolean existsByStartAt(LocalTime time) {
        Boolean result = jdbcTemplate.queryForObject("""
                        SELECT EXISTS(
                            SELECT *
                            FROM reservation_time
                            WHERE start_at = ?
                        )
                        """,
                Boolean.class,
                time
        );
        return Boolean.TRUE.equals(result);
    }

    @Transactional
    public ReservationTime save(ReservationTime reservationTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("start_at", reservationTime.getStartAt());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new ReservationTime(id, reservationTime.getStartAt());
    }

    @Transactional
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }

    public ReservationTime findTimeById(Long timeId) {
        return jdbcTemplate.queryForObject(
                "SELECT id, start_at FROM reservation_time WHERE id = ?",
                (rs, rowNum) -> new ReservationTime(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime()
                ),
                timeId
        );
    }

    public List<ReservationTimeStatus> findAvailableTime(Long id, String date) {
        return jdbcTemplate.query("""
                               SELECT t.id AS time_id, t.start_at,
                                      CASE WHEN r.id IS NULL THEN 'AVAILABLE' ELSE 'CONFIRMED' END AS status
                               FROM reservation_time t
                               LEFT JOIN reservation r ON t.id = r.time_id
                                   AND r.theme_id = ?
                                   AND r.date = ?
                                   AND r.status = 'CONFIRMED'
                               ORDER BY t.start_at
                        """,
                (rs, rowNum) -> {
                    ReservationTime time = new ReservationTime(
                            rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime());

                    return new ReservationTimeStatus(time, ReservationStatus.valueOf(rs.getString("status")));

                }, id, date
        );
    }
}
