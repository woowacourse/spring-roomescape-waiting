package roomescape.payment.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.exception.PaymentErrorCode;

@Repository
public class JdbcOrderRepository implements OrderRepository {

    private static final RowMapper<Order> ORDER_ROW_MAPPER = orderRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert orderInsert;

    public JdbcOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.orderInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("orders")
                .usingGeneratedKeyColumns("id")
                .usingColumns("reservation_id", "order_id", "amount", "payment_key", "idempotency_key", "status");
    }

    private static RowMapper<Order> orderRowMapper() {
        return (rs, rowNum) -> Order.of(
                rs.getLong("id"),
                rs.getLong("reservation_id"),
                rs.getString("order_id"),
                rs.getLong("amount"),
                rs.getString("payment_key"),
                rs.getString("idempotency_key"),
                PaymentStatus.valueOf(rs.getString("status"))
        );
    }

    @Override
    public Order save(Order order) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservation_id", order.getReservationId())
                .addValue("order_id", order.getOrderId().value())
                .addValue("amount", order.getAmount())
                .addValue("payment_key", order.getPaymentKey())
                .addValue("idempotency_key", order.getIdempotencyKey())
                .addValue("status", order.getStatus().name());
        long id = orderInsert.executeAndReturnKey(params).longValue();

        return Order.of(
                id,
                order.getReservationId(),
                order.getOrderId().value(),
                order.getAmount(),
                order.getPaymentKey(),
                order.getIdempotencyKey(),
                order.getStatus()
        );
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        String sql = """
                SELECT id, reservation_id, order_id, amount, payment_key, idempotency_key, status
                FROM orders
                WHERE order_id = :order_id
                """;

        List<Order> results = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("order_id", orderId),
                ORDER_ROW_MAPPER
        );

        return results.stream().findFirst();
    }

    @Override
    public Optional<Order> findByReservationId(Long reservationId) {
        String sql = """
                SELECT id, reservation_id, order_id, amount, payment_key, idempotency_key, status
                FROM orders
                WHERE reservation_id = :reservation_id
                """;

        List<Order> results = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("reservation_id", reservationId),
                ORDER_ROW_MAPPER
        );

        return results.stream().findFirst();
    }

    @Override
    public List<Order> findByReservationIds(List<Long> reservationIds) {
        if (reservationIds.isEmpty()) {
            return List.of();
        }
        String sql = """
                SELECT id, reservation_id, order_id, amount, payment_key, idempotency_key, status
                FROM orders
                WHERE reservation_id IN (:reservation_ids)
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("reservation_ids", reservationIds),
                ORDER_ROW_MAPPER
        );
    }

    @Override
    public Order updatePayment(Order order) {
        String sql = """
                UPDATE orders
                SET payment_key = :payment_key,
                    status      = :status
                WHERE id = :id
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("payment_key", order.getPaymentKey())
                        .addValue("status", order.getStatus().name())
                        .addValue("id", order.getId())
        );

        return findByOrderId(order.getOrderId().value())
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.ORDER_NOT_FOUND));
    }
}
