package roomescape.payment.repository;

import java.util.List;
import java.util.Optional;
import roomescape.payment.domain.Order;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findByOrderId(String orderId);

    Optional<Order> findByReservationId(Long reservationId);

    List<Order> findByReservationIds(List<Long> reservationIds);

    Order updatePayment(Order order);
}
