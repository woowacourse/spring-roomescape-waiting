package roomescape.reservation.infra;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.application.dto.ReservationDetail;
import roomescape.reservation.application.dao.ReservationDetailDao;

@RequiredArgsConstructor
@Repository
public class JdbcReservationDao implements ReservationDetailDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ReservationDetail> findAll() {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, r.theme_id, t.name as theme_name, t.description, t.thumbnail_img_url, r.time_id, rt.start_at, r.payment_status
                        FROM reservation r
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
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
                                 roomescape.payment.PaymentStatus.from(rs.getString("payment_status")))
        );
    }

    @Override
    public List<ReservationDetail> findByName(String username) {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, r.theme_id, t.name as theme_name, t.description, t.thumbnail_img_url, r.time_id, rt.start_at, r.payment_status
                        FROM reservation r
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
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
                                roomescape.payment.PaymentStatus.from(rs.getString("payment_status"))),
                username
        );
    }
}
