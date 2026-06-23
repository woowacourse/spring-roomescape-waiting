package roomescape.payment.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.payment.domain.Payment;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Payment> rowMapper = (rs, rowNum) -> Payment.restore(
            rs.getString("order_id"),
            rs.getLong("amount"),
            rs.getString("payment_key"),
            rs.getLong("reservation_id")
    );

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment");
    }

    @Override
    public Payment save(Payment payment) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("order_id", payment.getOrderId())
                .addValue("amount", payment.getAmount())
                .addValue("payment_key", payment.getPaymentKey())
                .addValue("reservation_id", payment.getReservationId());
        simpleJdbcInsert.execute(parameters);
        return payment;
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        String query = "SELECT * FROM payment WHERE order_id = ?";
        return jdbcTemplate.query(query, rowMapper, orderId).stream().findFirst();
    }

    @Override
    public void updatePaymentKey(String orderId, String paymentKey) {
        String query = "UPDATE payment SET payment_key = ? WHERE order_id = ?";
        jdbcTemplate.update(query, paymentKey, orderId);
    }
}
