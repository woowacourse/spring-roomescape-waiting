package roomescape.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.order.Order;

public interface OrderDao {
    Order insert(Order order);

    Optional<Order> findByOrderId(String orderId);

    Order update(Order order);

    List<Order> findExpiredPending(LocalDateTime threshold);
}
