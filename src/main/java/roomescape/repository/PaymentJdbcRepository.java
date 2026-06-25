package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.OrderStatus;
import roomescape.domain.Payment;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PaymentJdbcRepository implements PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Payment> paymentRowMapper = (rs, rowNum) -> new Payment(
            rs.getLong("id"),
            rs.getString("order_id"),
            rs.getLong("amount"),
            rs.getString("payment_key"),
            rs.getLong("reservation_id"),
            OrderStatus.valueOf(rs.getString("status"))
    );

    @Override
    public Payment save(Payment payment) {
        String sql = "INSERT INTO payment (order_id, amount, payment_key, reservation_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, payment.getOrderId());
            ps.setLong(2, payment.getAmount());
            ps.setString(3, payment.getPaymentKey());
            ps.setLong(4, payment.getReservationId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return new Payment(
                id,
                payment.getOrderId(),
                payment.getAmount(),
                payment.getPaymentKey(),
                payment.getReservationId()
        );
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        String sql = "SELECT id, order_id, amount, payment_key, reservation_id, status FROM payment WHERE order_id = ?";
        return jdbcTemplate.query(sql, paymentRowMapper, orderId).stream().findFirst();
    }

    @Override
    public List<Payment> findByReservationIds(Collection<Long> reservationIds) {
        if (reservationIds.isEmpty()) {
            return List.of();
        }
        String placeholders = reservationIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));
        String sql = "SELECT id, order_id, amount, payment_key, reservation_id, status FROM payment WHERE reservation_id IN ("
                + placeholders + ")";
        return jdbcTemplate.query(sql, paymentRowMapper, reservationIds.toArray());
    }

    @Override
    public void updatePaymentKey(String orderId, String paymentKey) {
        jdbcTemplate.update("UPDATE payment SET payment_key = ? WHERE order_id = ?", paymentKey, orderId);
    }

    @Override
    public void updateStatus(String orderId, OrderStatus status) {
        jdbcTemplate.update("UPDATE payment SET status = ? WHERE order_id = ?", status.name(), orderId);
    }
}
