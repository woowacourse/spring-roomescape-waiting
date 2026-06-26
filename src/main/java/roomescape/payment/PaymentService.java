package roomescape.payment;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.payment.order.PaymentOrder;
import roomescape.payment.order.PaymentOrderHistory;
import roomescape.payment.order.PaymentOrderRepository;

@Service
public class PaymentService {

    private final PaymentOrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentOrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        PaymentOrder order = orderRepository.getByOrderId(orderId);
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }
        return paymentGateway.confirm(
                new PaymentConfirmation(paymentKey, orderId, amount, order.getIdempotencyKey()));
    }

    public void cancel(String paymentKey, String reason) {
        paymentGateway.cancel(paymentKey, reason);
    }

    public List<PaymentOrderHistory> findOrderHistories(String reserverName) {
        return orderRepository.findHistoriesByReserverName(reserverName);
    }
}
