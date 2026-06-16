package roomescape.dao;

import static roomescape.dao.rowmapper.OrderMapper.ORDER_ROW_MAPPER;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.order.Order;
import roomescape.domain.order.OrderRepository;

@Repository
public class OrderDao implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Order order) {
        String sql = """
                INSERT INTO orders (id, amount, reservation_id) 
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                order.id(),
                order.amount(),
                order.reservation_id()
        );
    }

    @Override
    public Optional<Order> findById(String id) {
        String sql = """
                SELECT o.id, o.amount, o.reservation_id
                FROM orders o
                WHERE o.id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        ORDER_ROW_MAPPER,
                        id
                ).stream()
                .findFirst();
    }
}
