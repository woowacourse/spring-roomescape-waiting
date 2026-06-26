package roomescape.repository;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import roomescape.domain.PaymentOrder;

public class FakePaymentOrderRepository implements PaymentOrderRepository {

    private final Map<String, PaymentOrder> store = new LinkedHashMap<>();

    @Override
    public PaymentOrder save(PaymentOrder order) {
        store.put(order.orderId(), order);
        return order;
    }

    @Override
    public PaymentOrder update(PaymentOrder order) {
        store.put(order.orderId(), order);
        return order;
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public List<PaymentOrder> findByName(String name) {
        return store.values().stream()
                .filter(order -> Objects.equals(order.name(), name))
                .toList();
    }

    @Override
    public void deleteByOrderId(String orderId) {
        store.remove(orderId);
    }
}
