package roomescape.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.payment.PaymentOrder;
import roomescape.payment.PaymentOrderStatus;

@Repository
public class PaymentOrderDao {

    private static final RowMapper<PaymentOrder> paymentOrderRowMapper = (rs, rowNum) -> new PaymentOrder(
            rs.getLong("id"),
            rs.getString("order_id"),
            rs.getLong("user_id"),
            rs.getLong("schedule_id"),
            rs.getInt("amount"),
            PaymentOrderStatus.valueOf(rs.getString("status")),
            rs.getString("payment_key"),
            rs.getObject("reservation_id", Long.class),
            rs.getString("failure_code"),
            rs.getString("failure_message"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getObject("updated_at", LocalDateTime.class)
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public PaymentOrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(PaymentOrder order) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "order_id", order.getOrderId(),
                "user_id", order.getMemberId(),
                "schedule_id", order.getScheduleId(),
                "amount", order.getAmount(),
                "status", order.getStatus().name(),
                "created_at", order.getCreatedAt(),
                "updated_at", order.getUpdatedAt()
        )).longValue();
    }

    public Optional<PaymentOrder> findByOrderId(String orderId) {
        String sql = """
                SELECT id,
                       order_id,
                       user_id,
                       schedule_id,
                       amount,
                       status,
                       payment_key,
                       reservation_id,
                       failure_code,
                       failure_message,
                       created_at,
                       updated_at
                FROM payment_order
                WHERE order_id = ?
                """;
        List<PaymentOrder> results = jdbcTemplate.query(sql, paymentOrderRowMapper, orderId);
        return results.stream().findFirst();
    }

    public void confirm(String orderId, String paymentKey, Long reservationId, LocalDateTime updatedAt) {
        jdbcTemplate.update(
                """
                UPDATE payment_order
                SET status = ?,
                    payment_key = ?,
                    reservation_id = ?,
                    failure_code = NULL,
                    failure_message = NULL,
                    updated_at = ?
                WHERE order_id = ?
                """,
                PaymentOrderStatus.CONFIRMED.name(),
                paymentKey,
                reservationId,
                updatedAt,
                orderId
        );
    }

    public void fail(String orderId, String code, String message, LocalDateTime updatedAt) {
        jdbcTemplate.update(
                """
                UPDATE payment_order
                SET status = ?,
                    failure_code = ?,
                    failure_message = ?,
                    updated_at = ?
                WHERE order_id = ?
                  AND status = ?
                """,
                PaymentOrderStatus.FAILED.name(),
                code,
                message,
                updatedAt,
                orderId,
                PaymentOrderStatus.PENDING.name()
        );
    }
}
