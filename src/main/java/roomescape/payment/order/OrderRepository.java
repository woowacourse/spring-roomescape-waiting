package roomescape.payment.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.payment.OrderNotFoundException;
import roomescape.payment.PaymentStatus;

/**
 * 결제 주문 저장소.
 */
@Repository
public class OrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<Order> rowMapper = (rs, rowNum) -> new Order(
            rs.getLong("id"),
            rs.getString("order_id"),
            rs.getLong("reservation_id"),
            rs.getLong("amount"),
            rs.getString("payment_key"),
            PaymentStatus.valueOf(rs.getString("status")),
            rs.getString("idempotency_key"),
            rs.getObject("created_at", LocalDateTime.class)
    );

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    public Order save(Order order) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("order_id", order.orderId())
                .addValue("reservation_id", order.reservationId())
                .addValue("amount", order.amount())
                .addValue("payment_key", order.paymentKey())
                .addValue("status", order.status().name())
                .addValue("idempotency_key", order.idempotencyKey())
                .addValue("created_at", order.createdAt());

        long id = insertExecutor.executeAndReturnKey(params).longValue();
        return new Order(
                id,
                order.orderId(),
                order.reservationId(),
                order.amount(),
                order.paymentKey(),
                order.status(),
                order.idempotencyKey(),
                order.createdAt()
        );
    }

    public Optional<Order> findByOrderId(String orderId) {
        String sql = """
                SELECT id, order_id, reservation_id, amount, payment_key, status, idempotency_key, created_at
                FROM payment_order
                WHERE order_id = :orderId
                """;
        return jdbcTemplate.query(sql, Map.of("orderId", orderId), rowMapper)
                .stream()
                .findFirst();
    }

    public Optional<Order> findByReservationId(long reservationId) {
        String sql = """
                SELECT id, order_id, reservation_id, amount, payment_key, status, idempotency_key, created_at
                FROM payment_order
                WHERE reservation_id = :reservationId
                """;
        return jdbcTemplate.query(sql, Map.of("reservationId", reservationId), rowMapper)
                .stream()
                .findFirst();
    }

    public List<Order> findAll() {
        String sql = """
                SELECT id, order_id, reservation_id, amount, payment_key, status, idempotency_key, created_at
                FROM payment_order
                """;
        return jdbcTemplate.query(sql, Map.of(), rowMapper);
    }

    public void confirm(String orderId, String paymentKey, PaymentStatus status) {
        String sql = """
                UPDATE payment_order
                SET payment_key = :paymentKey, status = :status
                WHERE order_id = :orderId
                  AND status <> :doneStatus
                """;
        int affected = jdbcTemplate.update(sql, Map.of(
                "orderId", orderId,
                "paymentKey", paymentKey,
                "status", status.name(),
                "doneStatus", PaymentStatus.DONE.name()
        ));

        if (affected == 0 && findByOrderId(orderId).isEmpty()) {
            throw new OrderNotFoundException("요청한 결제 주문을 찾을 수 없습니다.");
        }
    }

    public void markUnknown(String orderId) {
        String sql = """
                UPDATE payment_order
                SET status = :status
                WHERE order_id = :orderId
                  AND status <> :doneStatus
                """;
        int affected = jdbcTemplate.update(sql, Map.of(
                "orderId", orderId,
                "status", PaymentStatus.UNKNOWN.name(),
                "doneStatus", PaymentStatus.DONE.name()
        ));

        if (affected == 0 && findByOrderId(orderId).isEmpty()) {
            throw new OrderNotFoundException("요청한 결제 주문을 찾을 수 없습니다.");
        }
    }
}
