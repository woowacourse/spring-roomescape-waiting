package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentOrderStatus;

import java.util.Optional;

@Repository
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPaymentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(PaymentOrder order) {
        String sql = """
                INSERT INTO payment_order (order_id, reservation_id, amount, idempotency_key, payment_key, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                order.orderId(),
                order.reservationId(),
                order.amount(),
                order.idempotencyKey(),
                order.paymentKey(),
                order.status().name()
        );
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        String sql = """
                SELECT order_id, reservation_id, amount, idempotency_key, payment_key, status
                FROM payment_order
                WHERE order_id = ?
                """;
        try {
            PaymentOrder order = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PaymentOrder(
                    rs.getString("order_id"),
                    rs.getLong("reservation_id"),
                    rs.getLong("amount"),
                    rs.getString("idempotency_key"),
                    rs.getString("payment_key"),
                    PaymentOrderStatus.valueOf(rs.getString("status"))
            ), orderId);
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PaymentOrder> findByReservationId(long reservationId) {
        String sql = """
                SELECT order_id, reservation_id, amount, idempotency_key, payment_key, status
                FROM payment_order
                WHERE reservation_id = ?
                """;
        try {
            PaymentOrder order = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PaymentOrder(
                    rs.getString("order_id"),
                    rs.getLong("reservation_id"),
                    rs.getLong("amount"),
                    rs.getString("idempotency_key"),
                    rs.getString("payment_key"),
                    PaymentOrderStatus.valueOf(rs.getString("status"))
            ), reservationId);
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void recordPaymentKey(String orderId, String paymentKey) {
        String sql = "UPDATE payment_order SET payment_key = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, paymentKey, orderId);
    }

    @Override
    public void complete(String orderId, String paymentKey) {
        String sql = "UPDATE payment_order SET payment_key = ?, status = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, paymentKey, PaymentOrderStatus.CONFIRMED.name(), orderId);
    }

    @Override
    public void markUnknown(String orderId) {
        String sql = "UPDATE payment_order SET status = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, PaymentOrderStatus.UNKNOWN.name(), orderId);
    }

    @Override
    public void markFailed(String orderId) {
        String sql = "UPDATE payment_order SET status = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, PaymentOrderStatus.FAILED.name(), orderId);
    }
}
