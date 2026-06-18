package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.payment.PaymentOrder;

@Repository
public class PaymentOrderJdbcRepository implements PaymentOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PaymentOrder> rowMapper = (resultSet, rowNum) -> new PaymentOrder(
            resultSet.getLong("id"),
            resultSet.getLong("reservation_id"),
            resultSet.getString("order_id"),
            resultSet.getLong("amount"),
            resultSet.getString("payment_key"),
            resultSet.getString("idempotency_key")
    );

    public PaymentOrderJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long save(PaymentOrder paymentOrder) {
        String sql = "insert into payment_order(reservation_id, order_id, amount, payment_key, idempotency_key) values(?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, paymentOrder.getReservationId());
            ps.setString(2, paymentOrder.getOrderId());
            ps.setLong(3, paymentOrder.getAmount());
            ps.setString(4, paymentOrder.getPaymentKey());
            ps.setString(5, paymentOrder.getIdempotencyKey());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        String sql = "select id, reservation_id, order_id, amount, payment_key, idempotency_key from payment_order where order_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, orderId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<PaymentOrder> findAllByReservationIds(List<Long> reservationIds) {
        if (reservationIds.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(", ", Collections.nCopies(reservationIds.size(), "?"));
        String sql = "select id, reservation_id, order_id, amount, payment_key, idempotency_key "
                + "from payment_order where reservation_id in (" + placeholders + ")";
        return jdbcTemplate.query(sql, rowMapper, reservationIds.toArray());
    }

    @Override
    public int updatePaymentKey(String orderId, String paymentKey) {
        String sql = "update payment_order set payment_key = ? where order_id = ?";
        return jdbcTemplate.update(sql, paymentKey, orderId);
    }

    @Override
    public int deleteByOrderId(String orderId) {
        String sql = "delete from payment_order where order_id = ?";
        return jdbcTemplate.update(sql, orderId);
    }
}
