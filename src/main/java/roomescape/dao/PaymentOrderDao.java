package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentOrderStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class PaymentOrderDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<PaymentOrder> ROW_MAPPER = (resultSet, rowNum) ->
            new PaymentOrder(
                    resultSet.getLong("id"),
                    resultSet.getString("order_id"),
                    resultSet.getLong("reservation_id"),
                    resultSet.getLong("amount"),
                    PaymentOrderStatus.valueOf(resultSet.getString("status")),
                    resultSet.getTimestamp("created_at").toLocalDateTime()
            );

    public PaymentOrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    public PaymentOrder insert(PaymentOrder paymentOrder) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("order_id", paymentOrder.getOrderId());
        parameters.put("reservation_id", paymentOrder.getReservationId());
        parameters.put("amount", paymentOrder.getAmount());
        parameters.put("status", paymentOrder.getStatus().name());
        parameters.put("created_at", paymentOrder.getCreatedAt());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new PaymentOrder(
                generatedId.longValue(),
                paymentOrder.getOrderId(),
                paymentOrder.getReservationId(),
                paymentOrder.getAmount(),
                paymentOrder.getStatus(),
                paymentOrder.getCreatedAt()
        );
    }

    public Optional<PaymentOrder> selectByOrderId(String orderId) {
        try {
            String sql = """
                    SELECT id, order_id, reservation_id, amount, status, created_at 
                    FROM payment_order 
                    WHERE order_id = ?
                    """;

            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, orderId));

        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
