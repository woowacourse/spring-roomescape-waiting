package roomescape.repository.jdbc;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.PaymentOrder;
import roomescape.repository.PaymentOrderRepository;

@Repository
@RequiredArgsConstructor
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public PaymentOrder save(PaymentOrder paymentOrder) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = """
                INSERT INTO payment_order (order_id, amount, entry_id, created_at)
                VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, paymentOrder.getOrderId());
            ps.setLong(2, paymentOrder.getAmount());
            ps.setLong(3, paymentOrder.getEntryId());
            ps.setTimestamp(4, Timestamp.valueOf(paymentOrder.getCreatedAt()));
            return ps;
        }, keyHolder);
        return paymentOrder.withId(keyHolder.getKey().longValue());
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        String sql = """
                SELECT id, order_id, amount, entry_id, created_at
                FROM payment_order
                WHERE order_id = ?
                """;
        List<PaymentOrder> result = jdbcTemplate.query(sql, (rs, rowNum) -> PaymentOrder.restore(
                rs.getLong("id"),
                rs.getString("order_id"),
                rs.getLong("amount"),
                rs.getLong("entry_id"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ), orderId);
        return result.stream().findFirst();
    }
}
