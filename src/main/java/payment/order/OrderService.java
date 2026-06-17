package payment.order;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private static final long RESERVATION_AMOUNT = 5_000L;

    private final OrderRepository orderRepository;

    public Order createReady(Long reservationId, LocalDateTime now) {
        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "");
        return orderRepository.save(Order.ready(orderId, reservationId, RESERVATION_AMOUNT, now));
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByReservationId(long reservationId) {
        return orderRepository.findByReservationId(reservationId);
    }
}
