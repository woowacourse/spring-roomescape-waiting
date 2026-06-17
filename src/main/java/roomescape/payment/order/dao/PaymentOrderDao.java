package roomescape.payment.order.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.payment.order.PaymentOrder;

@Repository
public class PaymentOrderDao {

    private final SimpleJdbcInsert simpleJdbcInsert;

    public PaymentOrderDao(JdbcTemplate jdbcTemplate) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    public PaymentOrder insert(String orderId, Long amount, Long reservationId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("order_id", orderId)
                .addValue("amount", amount)
                .addValue("reservation_id", reservationId);

        Long id = (long) simpleJdbcInsert.executeAndReturnKey(parameters);
        return new PaymentOrder(id, orderId, amount, reservationId);
    }

    public PaymentOrder selectByOrderId(String orderId) {
        String sql = "SELECT id, order_id, amount, reservation_id FROM payment_order WHERE order_id = ?";
        return simpleJdbcInsert.getJdbcTemplate().queryForObject(sql,
                (rs, rowNum) -> new PaymentOrder(
                        rs.getLong("id"),
                        rs.getString("order_id"),
                        rs.getLong("amount"),
                        rs.getLong("reservation_id")
                ), orderId);
    }
}
