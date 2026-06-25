package roomescape.repository;

import roomescape.domain.PendingPayment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakePendingPaymentRepository implements PendingPaymentRepository {

    private final Map<String, PendingPayment> store = new HashMap<>();

    @Override
    public PendingPayment save(PendingPayment pendingPayment) {
        store.put(pendingPayment.orderId(), pendingPayment);
        return pendingPayment;
    }

    @Override
    public Optional<PendingPayment> findByOrderId(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    // ponytail: no-op — cleanup is an integration concern, not unit
    public void deleteByOrderId(String orderId) {}
}
