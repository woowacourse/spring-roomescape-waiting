package roomescape.repository;

import roomescape.domain.payment.Payment;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FakePaymentRepository implements PaymentRepository {

    private final Map<Long, Payment> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public Payment save(Payment payment) {
        long id = sequence.getAndIncrement();
        Payment saved = new Payment(id, payment.getReservationId(), payment.getPaymentKey(), payment.getOrderId(), payment.getAmount(), payment.getStatus());
        storage.put(id, saved);
        return saved;
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return storage.values().stream()
                .filter(p -> Objects.equals(p.getOrderId(), orderId))
                .findFirst();
    }
}
