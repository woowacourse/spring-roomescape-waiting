package roomescape.payment.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.payment.domain.exception.OrderNotFoundException;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findByOrderId(String orderId);
    Optional<Order> findByReservationId(Long reservationId);
    int update(Order order);
    List<Order> findAllByName(String name);
    List<Order> findPendingOrdersBefore(LocalDateTime thresholdTime);

    default Order getByOrderId(String orderId) {
        return findByOrderId(orderId).orElseThrow(() -> new OrderNotFoundException("해당 주문을 찾을 수 없습니다."));
    }

    default Order getByReservationId(Long reservationId) {
        return findByReservationId(reservationId).orElseThrow(() -> new OrderNotFoundException("해당 주문을 찾을 수 없습니다."));
    }

    boolean existsByReservationId(Long reservationId);

}
