package roomescape.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.order.domain.Order;
import roomescape.order.repository.OrderRepository;
import roomescape.payment.client.PaymentGateway;
import roomescape.payment.repository.PaymentRepository;
import roomescape.payment.service.dto.PaymentConfirmation;
import roomescape.payment.service.dto.PaymentResult;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;

    public Order createOrder(Long reservationId, Long amount) {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(orderId, reservationId, amount);
        return orderRepository.save(order);
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        var order = orderRepository.getByOrderId(orderId);
        order.validateAmountMatch(amount);
        var confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        return paymentGateway.confirm(confirmation);
    }

}
