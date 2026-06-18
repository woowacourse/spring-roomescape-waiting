package roomescape.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderDao {
    Order insert(Order order);

    Optional<Order> findByOrderId(String orderId);

    Optional<Order> findPendingByReservationId(Long reservationId);

    Order update(Order order);

    List<Order> findExpiredPending(LocalDateTime threshold);
}
