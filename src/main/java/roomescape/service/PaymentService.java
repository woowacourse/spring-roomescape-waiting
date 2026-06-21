package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import roomescape.domain.Order;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.exception.GatewayTimeoutException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getAmount().equals(amount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        try {
            PaymentResult result = paymentGateway.confirm(
                    new PaymentConfirmation(paymentKey, orderId, order.getIdempotencyKey(), amount));
            orderRepository.confirm(order.getId(), result.paymentKey());
            return result;
        } catch (ResourceAccessException e) {
            orderRepository.markUncertain(order.getId());
            throw new GatewayTimeoutException(e);
        }
    }
}
