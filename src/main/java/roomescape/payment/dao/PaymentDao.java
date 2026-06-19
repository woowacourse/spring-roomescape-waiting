package roomescape.payment.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentDao {

  private final JdbcTemplate jdbcTemplate;

  public PaymentDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(Long reservationId, String paymentKey, String orderId, Long amount) {
    String sql = "INSERT INTO payment (reservation_id, payment_key, order_id, amount) VALUES (?, ?, ?, ?)";
    jdbcTemplate.update(sql, reservationId, paymentKey, orderId, amount);
  }
}
