package roomescape.dao.jdbc;

import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.OrderDao;
import roomescape.domain.payment.Order;
import roomescape.domain.payment.OrderStatus;

@Repository
public class OrderJdbcDao implements OrderDao {
    private static final RowMapper<Order> ROW_MAPPER = (rs, rowNum) -> Order.reconstruct(
            rs.getLong("id"),
            rs.getString("order_id"),
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
                .usingColumns("order_id", "reservation_id", "amount", "status");
    }

    @Override
    public Order insert(Order order) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("order_id", order.getOrderId())
                .addValue("reservation_id", order.getReservationId())
                .addValue("amount", order.getAmount())
                .addValue("status", order.getStatus().name());
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Order.reconstruct(id, order.getOrderId(), order.getReservationId(),
                order.getAmount(), order.getPaymentKey(), order.getStatus());
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        String sql = """
                SELECT id, order_id, reservation_id, amount, payment_key, status
                FROM orders
                WHERE order_id = :orderId
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("orderId", orderId), ROW_MAPPER)
                .stream().findFirst();
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
}
