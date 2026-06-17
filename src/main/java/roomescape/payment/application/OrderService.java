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

    private static final Long AMOUNT = 50000L;

    private final Clock clock;
    private final OrderRepository orderRepository;
    private final ActiveReservationRepository reservationRepository;

    public void createOrder(Long reservationId) {
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
        ActiveReservation activeReservation = reservationRepository.getById(reservationId);
        orderRepository.save(Order.createPending(orderId, AMOUNT, activeReservation, clock));
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
