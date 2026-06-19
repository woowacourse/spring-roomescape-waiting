package roomescape.persistence.jdbc.dao;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.order.Order;
import roomescape.domain.order.OrderAmount;
import roomescape.domain.order.OrderId;
import roomescape.domain.order.OrderName;
import roomescape.domain.order.OrderStatus;
import roomescape.domain.order.OrderType;
import roomescape.persistence.util.RepositoryExceptionTranslator;

@Repository
@RequiredArgsConstructor
public class OrderDao {

    private static final RowMapper<Order> ROW_MAPPER = (rs, rowNum) -> new Order(
            new OrderId(rs.getString("order_id")),
            rs.getLong("target_id"),
            OrderType.valueOf(rs.getString("order_type")),
            new OrderName(rs.getString("order_name")),
            new OrderAmount(rs.getLong("amount")),
            OrderStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    public Order save(Order order) {
        String sql = """
                MERGE INTO orders (
                    order_id, target_id, order_type, order_name, amount, status, created_at
                )
                KEY (order_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        RepositoryExceptionTranslator.execute(() ->
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setString(1, order.getOrderId().value());
                    ps.setLong(2, order.getTargetId());
                    ps.setString(3, order.getOrderType().name());
                    ps.setString(4, order.getOrderName().value());
                    ps.setLong(5, order.getAmount().value());
                    ps.setString(6, order.getStatus().name());
                    ps.setTimestamp(7, Timestamp.valueOf(order.getCreatedAt()));
                    return ps;
                }), "이미 존재하는 주문입니다.");

        return order;
    }

    public Optional<Order> findByOrderId(String orderId) {
        try {
            String sql = "SELECT * FROM orders WHERE order_id = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, ROW_MAPPER, orderId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
