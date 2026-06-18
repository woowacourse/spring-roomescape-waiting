package roomescape.reservation.infra;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.application.dao.PaymentHistoryDao;
import roomescape.reservation.application.dto.PaymentHistoryDetail;

@RequiredArgsConstructor
@Repository
public class JdbcPaymentHistoryDao implements PaymentHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<PaymentHistoryDetail> findByName(String username) {
        return jdbcTemplate.query(
                """
                        SELECT r.id AS reservation_id,
                               r.name,
                               r.date,
                               r.status AS reservation_status,
                               t.id AS theme_id,
                               t.name AS theme_name,
                               t.description,
                               t.thumbnail_img_url,
                               rt.id AS time_id,
                               rt.start_at,
                               p.order_id,
                               p.amount,
                               p.payment_key,
                               p.status AS payment_status
                        FROM payment p
                        JOIN reservation r ON p.reservation_id = r.id
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
                        WHERE r.name = ?
                        ORDER BY r.date ASC, rt.start_at ASC, p.id ASC
                        """,
                (rs, rowNum) -> new PaymentHistoryDetail(
                        rs.getLong("reservation_id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("description"),
                        rs.getString("thumbnail_img_url"),
                        rs.getLong("time_id"),
                        rs.getTime("start_at").toLocalTime(),
                        rs.getString("reservation_status"),
                        rs.getString("order_id"),
                        rs.getLong("amount"),
                        rs.getString("payment_key"),
                        rs.getString("payment_status")
                ),
                username
        );
    }
}
