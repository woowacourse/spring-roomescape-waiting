package roomescape.payment.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentStatus;

@Component
public class PaymentDao {

  private final JdbcTemplate jdbcTemplate;

  public PaymentDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(Long reservationId, String paymentKey, String orderId, Long amount, PaymentStatus status) {
    String sql = "INSERT INTO payment (reservation_id, payment_key, order_id, amount, status) VALUES (?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, reservationId, paymentKey, orderId, amount, status.name());
  }

  public int updateStatusIfUnknown(Long reservationId, PaymentStatus status) {
    String sql = "UPDATE payment SET status = ? WHERE reservation_id = ? AND status = 'UNKNOWN'";
    return jdbcTemplate.update(sql, status.name(), reservationId);
  }

  public Optional<Payment> findByReservationId(Long reservationId) {
    String sql = "SELECT id, reservation_id, payment_key, order_id, amount, status "
        + "FROM payment WHERE reservation_id = ?";
    List<Payment> result = jdbcTemplate.query(sql, (rs, rowNum) -> new Payment(
        rs.getLong("id"),
        rs.getLong("reservation_id"),
        rs.getString("payment_key"),
        rs.getString("order_id"),
        rs.getLong("amount"),
        PaymentStatus.from(rs.getString("status"))
    ), reservationId);

    return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
  }
}
