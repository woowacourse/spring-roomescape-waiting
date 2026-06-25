package roomescape.repository;

import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.PendingPayment;

@Repository
public class JdbcPendingPaymentRepository implements PendingPaymentRepository {

    private static final String FIND_SQL = "SELECT order_id, amount FROM pending_payment WHERE order_id = ?";
    private static final String DELETE_SQL = "DELETE FROM pending_payment WHERE order_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcPendingPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("pending_payment")
                .usingColumns("order_id", "amount");
    }

    @Override
    public PendingPayment save(PendingPayment pendingPayment) {
        jdbcInsert.execute(Map.of("order_id", pendingPayment.orderId(), "amount", pendingPayment.amount()));
        return pendingPayment;
    }

    @Override
    public Optional<PendingPayment> findByOrderId(String orderId) {
        return jdbcTemplate.query(FIND_SQL,
                (rs, x) -> new PendingPayment(rs.getString("order_id"), rs.getLong("amount")),
                orderId).stream().findAny();
    }

    @Override
    public void deleteByOrderId(String orderId) {
        jdbcTemplate.update(DELETE_SQL, orderId);
    }
}
