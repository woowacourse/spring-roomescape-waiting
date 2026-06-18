package roomescape.repository;

import java.util.Optional;
import roomescape.domain.Order;
import roomescape.domain.OrderId;
import roomescape.domain.PaymentStatus;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderId(OrderId orderId);

    Optional<Order> findByReservationId(Long reservationId);

    int updatePayment(OrderId orderId, PaymentStatus status, String paymentKey);

    int deleteByOrderId(OrderId orderId);
}