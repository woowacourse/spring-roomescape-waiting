package roomescape.payment.order.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.payment.controller.PaymentHistoryResponse;
import roomescape.payment.order.PaymentOrder;
import roomescape.payment.order.PaymentOrderStatus;

import java.util.List;

@Repository
public class PaymentOrderDao {

    private static final RowMapper<PaymentOrder> ORDER_MAPPER = (rs, rowNum) -> new PaymentOrder(
            rs.getLong("id"),
            rs.getString("order_id"),
            rs.getLong("amount"),
            rs.getLong("reservation_id"),
            rs.getString("idempotency_key"),
            PaymentOrderStatus.valueOf(rs.getString("status")),
            rs.getString("payment_key"),
            (Long) rs.getObject("approved_amount")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public PaymentOrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    public PaymentOrder insert(String orderId, Long amount, Long reservationId, String idempotencyKey) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("order_id", orderId)
                .addValue("amount", amount)
                .addValue("reservation_id", reservationId)
                .addValue("idempotency_key", idempotencyKey)
                .addValue("status", PaymentOrderStatus.PENDING.name());

        Long id = (long) simpleJdbcInsert.executeAndReturnKey(parameters);
        return new PaymentOrder(id, orderId, amount, reservationId, idempotencyKey,
                PaymentOrderStatus.PENDING, null, null);
    }

    public PaymentOrder selectByOrderId(String orderId) {
        String sql = "SELECT id, order_id, amount, reservation_id, idempotency_key, status, payment_key, approved_amount "
                + "FROM payment_order WHERE order_id = ?";
        return jdbcTemplate.queryForObject(sql, ORDER_MAPPER, orderId);
    }

    public void updateConfirmed(String orderId, String paymentKey, Long approvedAmount) {
        String sql = "UPDATE payment_order SET status = ?, payment_key = ?, approved_amount = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, PaymentOrderStatus.CONFIRMED.name(), paymentKey, approvedAmount, orderId);
    }

    public void updateStatus(String orderId, PaymentOrderStatus status) {
        String sql = "UPDATE payment_order SET status = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, status.name(), orderId);
    }

    public List<PaymentHistoryResponse> selectHistoryByName(String name) {
        String sql = """
                SELECT r.id AS reservation_id, t.name AS theme_name, r.date AS date,
                       rt.start_at AS start_at, po.status AS status, po.order_id AS order_id,
                       po.payment_key AS payment_key, po.amount AS amount, po.approved_amount AS approved_amount
                FROM payment_order po
                JOIN reservation r ON po.reservation_id = r.id
                JOIN theme t ON r.theme_id = t.id
                JOIN reservation_time rt ON r.time_id = rt.id
                WHERE r.name = ?
                ORDER BY r.date DESC, rt.start_at DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> PaymentHistoryResponse.of(
                rs.getLong("reservation_id"),
                rs.getString("theme_name"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("start_at").toLocalTime(),
                PaymentOrderStatus.valueOf(rs.getString("status")),
                rs.getString("order_id"),
                rs.getString("payment_key"),
                rs.getLong("amount"),
                (Long) rs.getObject("approved_amount")
        ), name);
    }
}
