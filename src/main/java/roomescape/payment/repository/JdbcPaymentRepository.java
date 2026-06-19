package roomescape.payment.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentStatus;

import java.util.Optional;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Payment> rowMapper = (rs, rowNum) -> Payment.load(
            rs.getLong("id"),
            rs.getLong("reservation_id"),
            rs.getLong("slot_id"),
            rs.getString("order_id"),
            rs.getString("payment_key"),
            rs.getLong("amount"),
            PaymentStatus.valueOf(rs.getString("status"))
    );

    public JdbcPaymentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("payment")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Payment save(Payment payment) {
        var params = new MapSqlParameterSource()
                .addValue("reservation_id", payment.getReservationId())
                .addValue("slot_id", payment.getSlotId())
                .addValue("order_id", payment.getOrderId())
                .addValue("payment_key", payment.getPaymentKey())
                .addValue("amount", payment.getAmount())
                .addValue("status", payment.getStatus().name());
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Payment.load(id, payment.getReservationId(), payment.getSlotId(), payment.getOrderId(),
                payment.getPaymentKey(), payment.getAmount(), payment.getStatus());
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        String sql = """
                SELECT id, reservation_id, slot_id, order_id, payment_key, amount, status
                FROM payment
                WHERE order_id = :orderId
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
                    new MapSqlParameterSource("orderId", orderId), rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean update(Payment payment) {
        String sql = """
                UPDATE payment
                SET status = :status, payment_key = :paymentKey
                WHERE id = :id
                """;
        var params = new MapSqlParameterSource()
                .addValue("status", payment.getStatus().name())
                .addValue("paymentKey", payment.getPaymentKey())
                .addValue("id", payment.getId());
        return jdbcTemplate.update(sql, params) > 0;
    }
}
