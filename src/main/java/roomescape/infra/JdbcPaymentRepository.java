package roomescape.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentStatus;
import roomescape.repository.PaymentRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Payment save(Payment payment) {
        Map<String, Object> params = new HashMap<>();
        params.put("reservation_id", payment.getReservationId());
        params.put("payment_key", payment.getPaymentKey());
        params.put("order_id", payment.getOrderId());
        params.put("amount", payment.getAmount());
        params.put("status", payment.getStatus().name());
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Payment(id, payment.getReservationId(), payment.getPaymentKey(), payment.getOrderId(), payment.getAmount(), payment.getStatus());
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        String sql = """
                SELECT id, reservation_id, payment_key, order_id, amount, status
                FROM payment
                WHERE order_id = ?
                """;
        return jdbcTemplate.query(sql, paymentRowMapper(), orderId).stream().findFirst();
    }

    @Override
    public Optional<Payment> findByReservationId(Long reservationId) {
        String sql = """
                SELECT id, reservation_id, payment_key, order_id, amount, status
                FROM payment
                WHERE reservation_id = ?
                """;
        return jdbcTemplate.query(sql, paymentRowMapper(), reservationId).stream().findFirst();
    }

    private org.springframework.jdbc.core.RowMapper<Payment> paymentRowMapper() {
        return (rs, rowNum) -> new Payment(
                rs.getLong("id"),
                rs.getLong("reservation_id"),
                rs.getString("payment_key"),
                rs.getString("order_id"),
                rs.getLong("amount"),
                PaymentStatus.valueOf(rs.getString("status"))
        );
    }
}
