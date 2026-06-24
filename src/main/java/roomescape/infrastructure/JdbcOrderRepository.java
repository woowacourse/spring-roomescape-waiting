package roomescape.infrastructure;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Order;
import roomescape.domain.repository.OrderRepository;

@Repository
public class JdbcOrderRepository implements OrderRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert orderInsert;

    public JdbcOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.orderInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public void findById(Long id) {
    }

    @Override
    public Order getById(String orderId) {
        String sql = """
                SELECT id,
                       reservation_id,
                       order_id,
                       amount
                FROM payment_order
                WHERE order_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Order(
                rs.getLong("id"),
                rs.getLong("reservation_id"),
                rs.getString("order_id"),
                rs.getLong("amount")
        ), orderId);
    }

    @Override
    public void save(Order order) {
        orderInsert.execute(Map.of(
                "reservation_id", order.getReservationId(),
                "order_id", order.getOrderId(),
                "amount", order.getAmount()
        ));
    }
}
