package roomescape.repository.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.payment.PaymentOrder;
import roomescape.repository.PaymentOrderRepository;

public class FakePaymentOrderRepository implements PaymentOrderRepository {

    private final Map<Long, PaymentOrder> store = new HashMap<>();
    private long nextId = 1L;

    @Override
    public Long save(PaymentOrder paymentOrder) {
        Long id = nextId++;
        store.put(id, paymentOrder.withId(id));
        return id;
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        return store.values().stream()
                .filter(paymentOrder -> paymentOrder.getOrderId().equals(orderId))
                .findFirst();
    }
}
