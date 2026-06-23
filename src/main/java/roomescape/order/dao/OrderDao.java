package roomescape.order.dao;

import java.sql.PreparedStatement;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import roomescape.exception.NotFoundException;
import roomescape.order.dao.dto.OrderRow;
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

  public OrderRow findByOrderId(String orderId) {
    try {
      String sql = "SELECT id, reservation_id, order_id, amount FROM orders WHERE order_id = ?";
      return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new OrderRow(
          rs.getLong("id"),
          rs.getLong("reservation_id"),
          rs.getString("order_id"),
          rs.getLong("amount")
      ), orderId);
    } catch (EmptyResultDataAccessException e) {
      throw new NotFoundException("존재하지 않는 주문입니다. orderId = " + orderId);
    }
  }

  public void deleteByOrderId(String orderId) {
    jdbcTemplate.update("DELETE FROM orders WHERE order_id = ?", orderId);
  }
}
