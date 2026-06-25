package roomescape.payment.order;

import java.sql.Date;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {

    private static final RowMapper<PaymentOrder> ROW_MAPPER = (rs, rowNum) -> new PaymentOrder(
            rs.getString("order_id"),
            rs.getString("reserver_name"),
            rs.getDate("date").toLocalDate(),
            rs.getLong("time_id"),
            rs.getLong("theme_id"),
            rs.getLong("amount"),
            rs.getString("payment_key"),
            PaymentOrderStatus.valueOf(rs.getString("status")),
            (Long) rs.getObject("reservation_id")
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcPaymentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(PaymentOrder order) {
        String sql = "INSERT INTO payment_order "
                + "(order_id, reserver_name, date, time_id, theme_id, amount, payment_key, status, reservation_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                order.getOrderId(),
                order.getReserverName(),
                Date.valueOf(order.getDate()),
                order.getTimeId(),
                order.getThemeId(),
                order.getAmount(),
                order.getPaymentKey(),
                order.getStatus().name(),
                order.getReservationId()
        );
    }

    @Override
    public PaymentOrder getByOrderId(String orderId) {
        String sql = "SELECT order_id, reserver_name, date, time_id, theme_id, amount, payment_key, status, reservation_id "
                + "FROM payment_order WHERE order_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, ROW_MAPPER, orderId);
        } catch (EmptyResultDataAccessException e) {
            throw new PaymentOrderNotFoundException("주문을 찾을 수 없습니다: orderId=" + orderId);
        }
    }

    @Override
    public void markConfirmed(String orderId, String paymentKey, Long reservationId) {
        jdbcTemplate.update(
                "UPDATE payment_order SET status = ?, payment_key = ?, reservation_id = ?, "
                        + "updated_at = CURRENT_TIMESTAMP WHERE order_id = ?",
                PaymentOrderStatus.CONFIRMED.name(), paymentKey, reservationId, orderId
        );
    }

    @Override
    public void markCanceled(String orderId) {
        jdbcTemplate.update(
                "UPDATE payment_order SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?",
                PaymentOrderStatus.CANCELED.name(), orderId
        );
    }

    @Override
    public void markUnknown(String orderId) {
        jdbcTemplate.update(
                "UPDATE payment_order SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?",
                PaymentOrderStatus.UNKNOWN.name(), orderId
        );
    }
}
