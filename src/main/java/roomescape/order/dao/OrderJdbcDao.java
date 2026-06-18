package roomescape.order.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.order.Order;
import roomescape.order.OrderDao;
import roomescape.order.OrderStatus;

@Repository
public class OrderJdbcDao implements OrderDao {
    private static final RowMapper<Order> ROW_MAPPER = (rs, rowNum) -> Order.reconstruct(
            rs.getLong("id"),
            rs.getString("order_id"),
            rs.getString("idempotency_key"),
            rs.getLong("reservation_id"),
            rs.getLong("amount"),
            rs.getString("payment_key"),
            OrderStatus.valueOf(rs.getString("status"))
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public OrderJdbcDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("orders")
                .usingGeneratedKeyColumns("id")
                .usingColumns("order_id", "idempotency_key", "reservation_id", "amount", "status");
    }

    @Override
    public Order insert(Order order) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("order_id", order.getOrderId())
                .addValue("idempotency_key", order.getIdempotencyKey())
                .addValue("reservation_id", order.getReservationId())
                .addValue("amount", order.getAmount())
                .addValue("status", order.getStatus().name());
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Order.reconstruct(id, order.getOrderId(), order.getIdempotencyKey(), order.getReservationId(),
                order.getAmount(), order.getPaymentKey(), order.getStatus());
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        String sql = """
                SELECT id, order_id, idempotency_key, reservation_id, amount, payment_key, status
                FROM orders
                WHERE order_id = :orderId
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("orderId", orderId), ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public Optional<Order> findPendingByReservationId(Long reservationId) {
        String sql = """
                SELECT id, order_id, idempotency_key, reservation_id, amount, payment_key, status
                FROM orders
                WHERE reservation_id = :reservationId AND status = :status
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId)
                .addValue("status", OrderStatus.PENDING.name());
        return jdbcTemplate.query(sql, params, ROW_MAPPER).stream().findFirst();
    }

    @Override
    public Order update(Order order) {
        String sql = """
                UPDATE orders
                SET payment_key = :paymentKey, status = :status
                WHERE order_id = :orderId
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentKey", order.getPaymentKey())
                .addValue("status", order.getStatus().name())
                .addValue("orderId", order.getOrderId());
        jdbcTemplate.update(sql, params);
        return order;
    }

    @Override
    public List<Order> findExpiredPending(LocalDateTime threshold) {
        String sql = """
                SELECT id, order_id, idempotency_key, reservation_id, amount, payment_key, status
                FROM orders
                WHERE status = :status AND created_at < :threshold
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", OrderStatus.PENDING.name())
                .addValue("threshold", threshold);
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    @Override
    public List<Order> findByReservationIds(List<Long> reservationIds) {
        String sql = """
                SELECT id, order_id, idempotency_key, reservation_id, amount, payment_key, status
                FROM orders
                WHERE reservation_id IN (:reservationIds)
                """;
        SqlParameterSource params = new MapSqlParameterSource("reservationIds", reservationIds);
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    @Override
    public int compareAndUpdate(Order order, OrderStatus expectedStatus) {
        String sql = """
                UPDATE orders
                SET payment_key = :paymentKey, status = :status
                WHERE order_id = :orderId AND status = :expected
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("paymentKey", order.getPaymentKey())
                .addValue("status", order.getStatus().name())
                .addValue("orderId", order.getOrderId())
                .addValue("expected", expectedStatus.name());
        return jdbcTemplate.update(sql, params);
    }

    @Override
    public List<Order> findNeedsCheck() {
        String sql = """
                SELECT id, order_id, idempotency_key, reservation_id, amount, payment_key, status
                FROM orders
                WHERE status = :status
                """;
        SqlParameterSource params = new MapSqlParameterSource("status", OrderStatus.NEEDS_CHECK.name());
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }
}
