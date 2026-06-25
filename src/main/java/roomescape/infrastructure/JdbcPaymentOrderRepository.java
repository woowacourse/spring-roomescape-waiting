package roomescape.infrastructure;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentStatus;
import roomescape.domain.repository.PaymentOrderRepository;

@Repository
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert paymentOrderInsert;

    public JdbcPaymentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.paymentOrderInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public PaymentOrder getByOrderId(String orderId) {
        String sql = """
                SELECT id,
                       reservation_id,
                       order_id,
                       amount,
                       payment_status
                FROM payment_order
                WHERE order_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PaymentOrder(
                rs.getLong("id"),
                rs.getLong("reservation_id"),
                rs.getString("order_id"),
                rs.getLong("amount"),
                PaymentStatus.valueOf(rs.getString("payment_status"))
        ), orderId);
    }

    @Override
    public void save(PaymentOrder paymentOrder) {
        paymentOrderInsert.execute(Map.of(
                "reservation_id", paymentOrder.getReservationId(),
                "order_id", paymentOrder.getOrderId(),
                "amount", paymentOrder.getAmount(),
                "payment_status", paymentOrder.getStatus().name()
        ));
    }

    @Override
    public void updateStatus(long id, PaymentStatus status) {
        jdbcTemplate.update("""
                        UPDATE payment_order
                        SET payment_status = ?
                        WHERE id = ?
                        """,
                status.name(),
                id
        );
    }
}
