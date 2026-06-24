package roomescape.infra.persistence;

import java.util.HashMap;
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
import roomescape.exception.NotFoundException;

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
                payment.getOrderId(),
                payment.getPaymentKey(),
                payment.getAmount(),
                payment.getReservationId()
        );
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        String sql = """
                SELECT id, order_id, payment_key, amount, reservation_id
                FROM payment
                WHERE order_id = ?
                """;
        List<Payment> payments = jdbcTemplate.query(sql, rowMapper(), orderId);
        return Optional.ofNullable(DataAccessUtils.singleResult(payments));
    }

    @Override
    public Payment updatePaymentKey(String orderId, String paymentKey) {
        String sql = "UPDATE payment SET payment_key = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, paymentKey, orderId);

        return findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("해당 결제 정보를 찾을 수 없습니다."));
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment")
                .usingColumns("order_id", "payment_key", "amount", "reservation_id")
                .usingGeneratedKeyColumns("id");
    }

    private Map<String, Object> createParams(Payment payment) {
        Map<String, Object> params = new HashMap<>();
        params.put("order_id", payment.getOrderId());
        params.put("payment_key", payment.getPaymentKey());
        params.put("amount", payment.getAmount());
        params.put("reservation_id", payment.getReservationId());
        return params;
    }

    private RowMapper<Payment> rowMapper() {
        return (rs, rowNum) -> new Payment(
                rs.getLong("id"),
                rs.getString("order_id"),
                rs.getString("payment_key"),
                rs.getLong("amount"),
                rs.getLong("reservation_id")
        );
    }
}
