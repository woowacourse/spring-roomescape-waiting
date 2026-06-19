package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.payment.Order;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.dto.payment.PaymentPrepareRequest;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.exception.PaymentUncertainException;
import roomescape.repository.OrderRepository;

import java.util.UUID;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    public void prepare(PaymentPrepareRequest request) {
        Order order = new Order(
                request.orderId(),
                request.amount(),
                request.name(),
                request.date(),
                request.timeId(),
                request.themeId(),
                UUID.randomUUID().toString()
        );
        orderRepository.save(order);
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Order order = orderRepository.getByOrderId(orderId);
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }
        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount, order.getIdempotencyKey());
        try {
            PaymentResult result = paymentGateway.confirm(confirmation);
            order.confirmSuccess(result.paymentKey());
            orderRepository.update(order);
            return result;
        } catch (PaymentUncertainException e) {
            order.markUncertain();
            orderRepository.update(order);
            throw e;
        } catch (RuntimeException e) {
            order.markFailed();
            orderRepository.update(order);
            throw e;
        }
    }

    public Order getOrder(String orderId) {
        return orderRepository.getByOrderId(orderId);
    }
}