package roomescape.adapter.persistence;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentStatus;
import roomescape.domain.repository.PaymentRepository;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Payment> ROW_MAPPER = (rs, rowNum) -> Payment.withId(
            rs.getLong("id"),
            rs.getLong("reservation_id"),
            rs.getString("order_id"),
            rs.getLong("amount"),
            rs.getString("payment_key"),
            PaymentStatus.valueOf(rs.getString("status"))
    );

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Payment save(Payment payment) {
        String sql = "INSERT INTO payment (reservation_id, order_id, amount, payment_key, status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, payment.getReservationId());
            ps.setString(2, payment.getOrderId());
            ps.setLong(3, payment.getAmount());
            ps.setString(4, payment.getPaymentKey());   // null 허용(대기)
            ps.setString(5, payment.getStatus().name());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return Payment.withId(id, payment.getReservationId(), payment.getOrderId(),
                payment.getAmount(), payment.getPaymentKey(), payment.getStatus());
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        String sql = "SELECT * FROM payment WHERE order_id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, orderId).stream().findFirst();
    }

    @Override
    public Optional<Payment> findByReservationId(Long reservationId) {
        String sql = "SELECT * FROM payment WHERE reservation_id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, reservationId).stream().findFirst();
    }

    @Override
    public void updateConfirmed(String orderId, String paymentKey, PaymentStatus status) {
        String sql = "UPDATE payment SET payment_key = ?, status = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, paymentKey, status.name(), orderId);
    }

    @Override
    public void deleteByReservationId(Long reservationId) {
        jdbcTemplate.update("DELETE FROM payment WHERE reservation_id = ?", reservationId);
    }

    @Override
    public void updateStatus(String orderId, PaymentStatus status) {
        jdbcTemplate.update("UPDATE payment SET status = ? WHERE order_id = ?", status.name(), orderId);
    }

}
