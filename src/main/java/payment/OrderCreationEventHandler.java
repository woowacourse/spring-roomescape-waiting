package payment;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import payment.order.Order;
import payment.order.OrderRepository;

@Component
@RequiredArgsConstructor
public class OrderCreationEventHandler {

    private static final long RESERVATION_AMOUNT = 5_000L;

    private final OrderRepository orderRepository;

    @EventListener
    public void createOrder(ReservationPendingPaymentEvent event) {
        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "");
        orderRepository.save(Order.ready(orderId, event.reservationId(), RESERVATION_AMOUNT, event.createdAt()));
    }
}
