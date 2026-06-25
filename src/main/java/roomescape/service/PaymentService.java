package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.OrderStatus;
import roomescape.domain.Payment;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.exception.NotFoundException;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.repository.PaymentRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentRepository paymentRepository, PaymentGateway paymentGateway) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public Payment createOrder(Long reservationId, long amount) {
        String orderId = generateOrderId();
        return paymentRepository.save(new Payment(orderId, amount, reservationId));
    }

    public Optional<Payment> findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> findByReservationIds(Collection<Long> reservationIds) {
        return paymentRepository.findByReservationIds(reservationIds);
    }

    @Transactional
    public Payment confirm(String paymentKey, String orderId, long amount) {
        Payment order = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> NotFoundException.payment(orderId));
        if (order.getAmount() != amount) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }

        paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));

        paymentRepository.updatePaymentKey(orderId, paymentKey);
        paymentRepository.updateStatus(orderId, OrderStatus.CONFIRMED);
        return order.withPaymentKey(paymentKey);
    }

    @Transactional
    public void markUncertain(String orderId) {
        paymentRepository.updateStatus(orderId, OrderStatus.UNCERTAIN);
    }

    @Transactional
    public void markFailed(String orderId) {
        paymentRepository.updateStatus(orderId, OrderStatus.FAILED);
    }

    private String generateOrderId() {
        return "order-" + UUID.randomUUID().toString().replace("-", "");
    }
}
