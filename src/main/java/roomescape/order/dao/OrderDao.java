package roomescape.order.dao;

import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import roomescape.order.domain.Order;

@Component
public class OrderDao {

  private final JdbcTemplate jdbcTemplate;

  public OrderDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Order insert(Long reservationId, String orderId, Long amount) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    String sql = "INSERT INTO orders (reservation_id, order_id, amount) VALUES (?, ?, ?)";

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
      ps.setLong(1, reservationId);
      ps.setString(2, orderId);
      ps.setLong(3, amount);
      return ps;
    }, keyHolder);

    return new Order(keyHolder.getKey().longValue(), orderId, amount);
  }
}
