package roomescape.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.dto.projection.ReservationTimeStatusProjection;

@Repository
@Transactional(readOnly = true)
public class TempReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public TempReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public List<ReservationTimeStatusProjection> findAvailableTime(Long id, String date) {
        return jdbcTemplate.query("""
                       SELECT t.id AS time_id, t.start_at,
                              CASE WHEN EXISTS (
                                       SELECT 1
                                       FROM reservation r
                                       WHERE r.time_id = t.id
                                           AND r.theme_id = ?
                                           AND r.date = ?
                                   ) THEN 'CONFIRMED' ELSE 'AVAILABLE' END AS status
                       FROM reservation_time t
                       ORDER BY t.start_at
                """, (rs, rowNum) -> {
            ReservationTime time = new ReservationTime(rs.getTime("start_at").toLocalTime());

            return new ReservationTimeStatusProjection(time, ReservationStatus.valueOf(rs.getString("status")));

        }, id, date);
    }
}
