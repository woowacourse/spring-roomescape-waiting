package roomescape.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentStatus;

@Repository
public class PaymentOrderDao {

    private static final RowMapper<PaymentOrder> ROW_MAPPER = (rs, rowNum) -> new PaymentOrder(
            rs.getLong("id"),
            rs.getString("order_id"),
            rs.getLong("amount"),
            PaymentStatus.valueOf(rs.getString("status")),
            rs.getLong("reservation_id"),
            rs.getString("payment_key")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public PaymentOrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    public PaymentOrder save(PaymentOrder paymentOrder) {
        Map<String, Object> params = new HashMap<>();
        params.put("order_id", paymentOrder.getOrderId());
        params.put("amount", paymentOrder.getAmount());
        params.put("status", paymentOrder.getStatus().name());
        params.put("reservation_id", paymentOrder.getReservationId());
        params.put("payment_key", paymentOrder.getPaymentKey());

        Number generatedId = jdbcInsert.executeAndReturnKey(params);
        return paymentOrder.createWithId(generatedId.longValue());
    }

    public Optional<PaymentOrder> findByOrderId(String orderId) {
        String sql = """
                SELECT id, 
                       order_id,
                       amount, 
                       status, 
                       payment_key, 
                       reservation_id
                FROM payment_order
                WHERE order_id = ?
                """;
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, orderId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void confirm(PaymentOrder paymentOrder) {
        String sql = """
                UPDATE payment_order
                SET status = ?,
                    payment_key = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                PaymentStatus.CONFIRMED.name(),
                paymentOrder.getPaymentKey(),
                paymentOrder.getId());
    }

    public void deleteByReservationId(long reservationId) {
        String sql = """
                DELETE FROM payment_order
                WHERE reservation_id = ?
                """;
        jdbcTemplate.update(sql, reservationId);
    }
}
