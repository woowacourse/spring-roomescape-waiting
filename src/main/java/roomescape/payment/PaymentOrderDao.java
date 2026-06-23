package roomescape.payment;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentOrderDao {

    private final JdbcTemplate jdbcTemplate;

    public PaymentOrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PaymentOrder insert(PaymentOrder order) {
        String sql = "insert into payment_order (order_id, reservation_id, amount, payment_key, status) "
                + "values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, order.getOrderId());
            statement.setLong(2, order.getReservationId());
            statement.setLong(3, order.getAmount());
            statement.setString(4, order.getPaymentKey());
            statement.setString(5, order.getStatus().name());
            return statement;
        }, keyHolder);
        return findByOrderId(order.getOrderId()).orElseThrow();
    }

    public Optional<PaymentOrder> findByOrderId(String orderId) {
        String sql = "select id, order_id, reservation_id, amount, payment_key, status, created_at "
                + "from payment_order where order_id = ?";
        return jdbcTemplate.query(sql, rowMapper(), orderId).stream().findFirst();
    }

    public Optional<PaymentOrder> findByOrderIdForUpdate(String orderId) {
        String sql = "select id, order_id, reservation_id, amount, payment_key, status, created_at "
                + "from payment_order where order_id = ? for update";
        return jdbcTemplate.query(sql, rowMapper(), orderId).stream().findFirst();
    }

    public PaymentOrder update(PaymentOrder order) {
        String sql = "update payment_order set payment_key = ?, status = ? where id = ?";
        jdbcTemplate.update(sql, order.getPaymentKey(), order.getStatus().name(), order.getId());
        return order;
    }

    public int deleteByOrderId(String orderId) {
        return jdbcTemplate.update("delete from payment_order where order_id = ?", orderId);
    }

    public int deleteByReservationId(Long reservationId) {
        return jdbcTemplate.update("delete from payment_order where reservation_id = ?", reservationId);
    }

    private RowMapper<PaymentOrder> rowMapper() {
        return (resultSet, rowNum) -> new PaymentOrder(
                resultSet.getLong("id"),
                resultSet.getString("order_id"),
                resultSet.getLong("reservation_id"),
                resultSet.getLong("amount"),
                resultSet.getString("payment_key"),
                PaymentOrderStatus.valueOf(resultSet.getString("status")),
                resultSet.getObject("created_at", LocalDateTime.class)
        );
    }
}
