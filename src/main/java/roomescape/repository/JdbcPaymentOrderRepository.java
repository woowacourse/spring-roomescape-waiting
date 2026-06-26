package roomescape.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentOrderStatus;

@Repository
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {

    private static final String COLUMNS = "order_id, amount, idempotency_key, status, name, session_id, payment_key";
    private static final String FIND_BY_ID_SQL =
            "SELECT " + COLUMNS + " FROM payment_order WHERE order_id = ?";
    private static final String FIND_BY_NAME_SQL =
            "SELECT " + COLUMNS + " FROM payment_order WHERE name = ? ORDER BY created_at DESC";
    private static final String UPDATE_SQL =
            "UPDATE payment_order SET status = ?, name = ?, session_id = ?, payment_key = ? WHERE order_id = ?";
    private static final String DELETE_SQL = "DELETE FROM payment_order WHERE order_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<PaymentOrder> rowMapper = (rs, rowNum) -> new PaymentOrder(
            rs.getString("order_id"),
            rs.getLong("amount"),
            rs.getString("idempotency_key"),
            PaymentOrderStatus.valueOf(rs.getString("status")),
            rs.getString("name"),
            (Long) rs.getObject("session_id"),
            rs.getString("payment_key")
    );

    public JdbcPaymentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingColumns("order_id", "amount", "idempotency_key", "status", "name", "session_id", "payment_key");
    }

    @Override
    public PaymentOrder save(PaymentOrder order) {
        Map<String, Object> params = new HashMap<>();
        params.put("order_id", order.orderId());
        params.put("amount", order.amount());
        params.put("idempotency_key", order.idempotencyKey());
        params.put("status", order.status().name());
        params.put("name", order.name());
        params.put("session_id", order.sessionId());
        params.put("payment_key", order.paymentKey());
        jdbcInsert.execute(params);
        return order;
    }

    @Override
    public PaymentOrder update(PaymentOrder order) {
        jdbcTemplate.update(UPDATE_SQL,
                order.status().name(), order.name(), order.sessionId(), order.paymentKey(), order.orderId());
        return order;
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, rowMapper, orderId).stream().findAny();
    }

    @Override
    public List<PaymentOrder> findByName(String name) {
        return jdbcTemplate.query(FIND_BY_NAME_SQL, rowMapper, name);
    }

    @Override
    public void deleteByOrderId(String orderId) {
        jdbcTemplate.update(DELETE_SQL, orderId);
    }
}
