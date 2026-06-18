package roomescape.reservation.infra;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.application.dao.ReservationDetailDao;
import roomescape.reservation.application.dto.ReservationDetail;
import roomescape.reservation.domain.ReservationStatus;

@RequiredArgsConstructor
@Repository
public class JdbcReservationDao implements ReservationDetailDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ReservationDetail> findAllByPage(int limit, long offset) {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, r.theme_id, t.name as theme_name, t.description, t.thumbnail_img_url, r.time_id, rt.start_at, r.status, p.order_id, p.amount
                        FROM reservation r
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
                        LEFT JOIN payment p ON p.reservation_id = r.id
                            AND p.status = 'PENDING'
                        ORDER BY r.date ASC, rt.start_at ASC, r.id ASC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) ->
                        new ReservationDetail(rs.getLong("id"),
                                rs.getString("name"),
                                rs.getDate("date").toLocalDate(),
                                rs.getLong("theme_id"),
                                rs.getString("theme_name"),
                                rs.getString("description"),
                                rs.getString("thumbnail_img_url"),
                                rs.getLong("time_id"),
                                rs.getTime("start_at").toLocalTime(),
                                ReservationStatus.valueOf(rs.getString("status")),
                                rs.getString("order_id"),
                                rs.getObject("amount", Long.class)),
                limit,
                offset
        );
    }

    @Override
    public long countAll() {
        Long count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM reservation
                        """,
                Long.class
        );

        return count;
    }

    @Override
    public List<ReservationDetail> findByName(String username) {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, r.theme_id, t.name as theme_name, t.description, t.thumbnail_img_url, r.time_id, rt.start_at, r.status, p.order_id, p.amount
                        FROM reservation r
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
                        LEFT JOIN payment p ON p.reservation_id = r.id
                            AND p.status = 'PENDING'
                        WHERE r.name = ?
                        ORDER BY r.date ASC
                        """,
                (rs, rowNum) ->
                        new ReservationDetail(rs.getLong("id"),
                                rs.getString("name"),
                                rs.getDate("date").toLocalDate(),
                                rs.getLong("theme_id"),
                                rs.getString("theme_name"),
                                rs.getString("description"),
                                rs.getString("thumbnail_img_url"),
                                rs.getLong("time_id"),
                                rs.getTime("start_at").toLocalTime(),
                                ReservationStatus.valueOf(rs.getString("status")),
                                rs.getString("order_id"),
                                rs.getObject("amount", Long.class)),
                username
        );
    }
}
