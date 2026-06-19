package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.PaymentOrder;

import java.util.Optional;

@Repository
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPaymentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(PaymentOrder order) {
        String sql = "INSERT INTO payment_order (order_id, reservation_id, amount, payment_key) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, order.orderId(), order.reservationId(), order.amount(), order.paymentKey());
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        String sql = "SELECT order_id, reservation_id, amount, payment_key FROM payment_order WHERE order_id = ?";
        try {
            PaymentOrder order = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PaymentOrder(
                    rs.getString("order_id"),
                    rs.getLong("reservation_id"),
                    rs.getLong("amount"),
                    rs.getString("payment_key")
            ), orderId);
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PaymentOrder> findByReservationId(long reservationId) {
        String sql = "SELECT order_id, reservation_id, amount, payment_key FROM payment_order WHERE reservation_id = ?";
        try {
            PaymentOrder order = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PaymentOrder(
                    rs.getString("order_id"),
                    rs.getLong("reservation_id"),
                    rs.getLong("amount"),
                    rs.getString("payment_key")
            ), reservationId);
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void complete(String orderId, String paymentKey) {
        String sql = "UPDATE payment_order SET payment_key = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, paymentKey, orderId);
    }

    @Override
    public void deleteByOrderId(String orderId) {
        String sql = "DELETE FROM payment_order WHERE order_id = ?";
        jdbcTemplate.update(sql, orderId);
    }
}
