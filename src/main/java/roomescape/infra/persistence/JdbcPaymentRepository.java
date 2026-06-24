package roomescape.infra.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentRepository;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Payment save(Payment payment) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(payment);
        long id = insert.executeAndReturnKey(params).longValue();

        return new Payment(
                id,
                payment.getPaymentKey(),
                payment.getOrderId()
        );
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        String sql = """
                SELECT id, payment_key, order_id
                FROM payment
                WHERE order_id = ?
                """;
        List<Payment> payments = jdbcTemplate.query(sql, rowMapper(), orderId);
        return Optional.ofNullable(DataAccessUtils.singleResult(payments));
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment")
                .usingColumns("payment_key", "order_id")
                .usingGeneratedKeyColumns("id");
    }

    private Map<String, Object> createParams(Payment payment) {
        return Map.of(
                "payment_key", payment.getPaymentKey(),
                "order_id", payment.getOrderId()
        );
    }

    private RowMapper<Payment> rowMapper() {
        return (rs, rowNum) -> new Payment(
                rs.getLong("id"),
                rs.getString("payment_key"),
                rs.getString("order_id")
        );
    }
}
