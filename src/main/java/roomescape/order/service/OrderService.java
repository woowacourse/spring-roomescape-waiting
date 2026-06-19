package roomescape.order.service;

import org.springframework.stereotype.Service;
import roomescape.order.dao.OrderDao;
import roomescape.order.dao.dto.OrderRow;
import roomescape.order.domain.Order;

@Service
public class OrderService {

  private final OrderDao orderDao;

  public OrderService(OrderDao orderDao) {
    this.orderDao = orderDao;
  }

  public Order save(Long reservationId, String orderId, Long amount) {
    return orderDao.insert(reservationId, orderId, amount);
  }

  public OrderRow findByOrderId(String orderId) {
    return orderDao.findByOrderId(orderId);
  }
}
