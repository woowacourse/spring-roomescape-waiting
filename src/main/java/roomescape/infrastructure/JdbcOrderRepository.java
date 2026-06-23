package roomescape.infrastructure;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Order;
import roomescape.domain.repository.OrderRepository;

@Repository
public class JdbcOrderRepository implements OrderRepository {
    private final SimpleJdbcInsert orderInsert;

    public JdbcOrderRepository(JdbcTemplate jdbcTemplate) {
        this.orderInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
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
