package roomescape.payment.order;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.OrderTicket;
import roomescape.payment.PaymentOrderPort;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService implements PaymentOrderPort {

    private static final long RESERVATION_AMOUNT = 5_000L;

    private final OrderRepository orderRepository;
    private final Clock clock;

    @Override
    @Transactional
    public OrderTicket placeOrder(long reservationId) {
        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "");
        Order saved = orderRepository.save(
                Order.ready(orderId, reservationId, RESERVATION_AMOUNT, LocalDateTime.now(clock)));
        return new OrderTicket(saved.orderId(), saved.amount());
    }

    @Override
    public Optional<OrderTicket> findTicket(long reservationId) {
        return orderRepository.findByReservationId(reservationId)
                .map(order -> new OrderTicket(order.orderId(), order.amount()));
    }
}
