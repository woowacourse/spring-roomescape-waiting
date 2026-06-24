package roomescape.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentRepository;

public class FakePaymentRepository implements PaymentRepository {

    private final Map<Long, Payment> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public Payment save(Payment payment) {
        long id = sequence++;
        Payment savedPayment = new Payment(id, payment.getPaymentKey(), payment.getOrderId());
        storage.put(id, savedPayment);
        return savedPayment;
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return storage.values().stream()
                .filter(payment -> payment.getOrderId().equals(orderId))
                .findAny();
    }
}
