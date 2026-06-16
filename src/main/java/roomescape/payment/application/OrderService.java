package roomescape.payment.application;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.OrderRepository;
import roomescape.payment.application.dto.OrderInfo;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.ActiveReservationRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final Clock clock;
    private final OrderRepository orderRepository;
    private final ActiveReservationRepository reservationRepository;

    public OrderInfo createOrder(Long reservationId, Long amount) {
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
        ActiveReservation activeReservation = reservationRepository.getById(reservationId);
        Order order = orderRepository.save(Order.createPending(orderId, amount, activeReservation, clock));
        return OrderInfo.from(order);
    }

    public OrderInfo getOrder(Long reservationId) {
        Order order = orderRepository.getByReservationId(reservationId);
        return OrderInfo.from(order);
    }

    public OrderInfo getOrder(String orderId) {
        Order order = orderRepository.getByOrderId(orderId);
        return OrderInfo.from(order);
    }

    public List<OrderInfo> getOrdersByName(String name) {
        List<Order> orders = orderRepository.findAllByName(name);
        return orders.stream().map(OrderInfo::from).toList();
    }
}
