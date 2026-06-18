package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Order;
import roomescape.domain.OrderId;
import roomescape.domain.PaymentStatus;

@Repository
public class OrderJdbcRepository implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Order> rowMapper = (resultSet, rowNum) -> new Order(
            resultSet.getLong("id"),
            OrderId.of(resultSet.getString("order_id")),
            resultSet.getLong("reservation_id"),
            PaymentStatus.valueOf(resultSet.getString("status")),
            resultSet.getString("payment_key")
    );

    public OrderJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Order save(Order order) {
        String sql = "insert into orders(order_id, reservation_id, amount, status) values(?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, order.getOrderId().getValue());
            ps.setLong(2, order.getReservationId());
            ps.setLong(3, order.getAmount());
            ps.setString(4, order.getStatus().name());
            return ps;
        }, keyHolder);

        return order.withId(keyHolder.getKey().longValue());
    }

    @Override
    public Optional<Order> findById(Long id) {
        String sql = """
                select id, order_id, reservation_id, amount, status, payment_key
                from orders
                where id = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Order> findByOrderId(OrderId orderId) {
        String sql = """
                select id, order_id, reservation_id, amount, status, payment_key
                from orders
                where order_id = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, orderId.getValue()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Order> findByReservationId(Long reservationId) {
        String sql = """
                select id, order_id, reservation_id, amount, status, payment_key
                from orders
                where reservation_id = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, reservationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public int updatePayment(OrderId orderId, PaymentStatus status, String paymentKey) {
        String sql = "update orders set status = ?, payment_key = ? where order_id = ?";
        return jdbcTemplate.update(sql, status.name(), paymentKey, orderId.getValue());
    }

    @Override
    public int deleteByOrderId(OrderId orderId) {
        String sql = "delete from orders where order_id = ?";
        return jdbcTemplate.update(sql, orderId.getValue());
    }
}