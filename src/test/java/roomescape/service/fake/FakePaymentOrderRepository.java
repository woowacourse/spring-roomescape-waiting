package roomescape.service.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.PaymentOrder;
import roomescape.repository.PaymentOrderRepository;

public class FakePaymentOrderRepository implements PaymentOrderRepository {

    private final Map<Long, PaymentOrder> storage = new HashMap<>();
    private final Map<String, PaymentOrder> orderIdIndex = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public PaymentOrder save(PaymentOrder paymentOrder) {
        Long id = paymentOrder.getId() == null ? counter.getAndIncrement() : paymentOrder.getId();
        PaymentOrder saved = paymentOrder.withId(id);
        storage.put(id, saved);
        orderIdIndex.put(saved.getOrderId(), saved);
        return saved;
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        return Optional.ofNullable(orderIdIndex.get(orderId));
    }
}
