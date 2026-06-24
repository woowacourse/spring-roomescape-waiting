package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Payment;

import java.sql.PreparedStatement;
import java.util.Optional;

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
            rs.getLong("reservation_id")
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
        String sql = "SELECT id, order_id, amount, payment_key, reservation_id FROM payment WHERE order_id = ?";
        return jdbcTemplate.query(sql, paymentRowMapper, orderId).stream().findFirst();
    }

    @Override
    public void updatePaymentKey(String orderId, String paymentKey) {
        jdbcTemplate.update("UPDATE payment SET payment_key = ? WHERE order_id = ?", paymentKey, orderId);
    }
}
