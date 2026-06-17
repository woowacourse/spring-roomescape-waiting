package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Payment;
import roomescape.service.exception.ResourceNotFoundException;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final RowMapper<Payment> paymentRowMapper =
            (rs, rowNum) -> new Payment(
                    rs.getLong("id"),
                    rs.getLong("reservation_id"),
                    rs.getString("order_id"),
                    rs.getLong("amount"),
                    rs.getString("payment_key")
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Payment save(Payment payment) {
        String sql = "INSERT INTO payment (reservation_id, order_id, amount, payment_key) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, payment.reservationId());
            ps.setString(2, payment.orderId());
            ps.setLong(3, payment.amount());
            if (payment.paymentKey() == null) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, payment.paymentKey());
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new Payment(
                id,
                payment.reservationId(),
                payment.orderId(),
                payment.amount(),
                payment.paymentKey()
        );
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        String sql = "SELECT id, reservation_id, order_id, amount, payment_key FROM payment WHERE order_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, paymentRowMapper, orderId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updatePaymentKey(String orderId, String paymentKey) {
        int affectedRows = jdbcTemplate.update(
                "UPDATE payment SET payment_key = ? WHERE order_id = ?",
                paymentKey, orderId
        );
        if (affectedRows == 0) {
            throw new ResourceNotFoundException("주문을 찾을 수 없습니다: orderId=" + orderId);
        }
    }

    @Override
    public void deleteByOrderId(String orderId) {
        jdbcTemplate.update("DELETE FROM payment WHERE order_id = ?", orderId);
    }
}
