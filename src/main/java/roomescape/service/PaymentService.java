package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Payment;
import roomescape.repository.PaymentRepository;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment createOrder(Long reservationId, long amount) {
        String orderId = generateOrderId();
        return paymentRepository.save(new Payment(orderId, amount, reservationId));
    }

    private String generateOrderId() {
        return "order-" + UUID.randomUUID().toString().replace("-", "");
    }
}
