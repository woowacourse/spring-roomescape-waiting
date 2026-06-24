package roomescape.infra.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.payment.Order;
import roomescape.domain.payment.OrderRepository;
import roomescape.domain.payment.OrderStatus;
import roomescape.exception.NotFoundException;

@Repository
public class JdbcOrderRepository implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Order save(Order order) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(order);
        long id = insert.executeAndReturnKey(params).longValue();

        return new Order(
                id,
                order.getOrderId(),
                order.getAmount(),
                order.getReservationId(),
                order.getStatus()
        );
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        String sql = """
                SELECT id, order_id, amount, reservation_id, status
                FROM orders
                WHERE order_id = ?
                """;
        List<Order> orders = jdbcTemplate.query(sql, rowMapper(), orderId);
        return Optional.ofNullable(DataAccessUtils.singleResult(orders));
    }

    @Override
    public Order updateStatus(String orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, status.name(), orderId);

        return findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("해당 주문 정보를 찾을 수 없습니다."));
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("orders")
                .usingColumns("order_id", "amount", "reservation_id", "status")
                .usingGeneratedKeyColumns("id");
    }

    private Map<String, Object> createParams(Order order) {
        return Map.of(
                "order_id", order.getOrderId(),
                "amount", order.getAmount(),
                "reservation_id", order.getReservationId(),
                "status", order.getStatus().name()
        );
    }

    private RowMapper<Order> rowMapper() {
        return (rs, rowNum) -> new Order(
                rs.getLong("id"),
                rs.getString("order_id"),
                rs.getLong("amount"),
                rs.getLong("reservation_id"),
                OrderStatus.valueOf(rs.getString("status"))
        );
    }
}
