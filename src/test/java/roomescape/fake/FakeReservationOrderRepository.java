package roomescape.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.domain.reservationOrder.ReservationOrderRepository;

public class FakeReservationOrderRepository implements ReservationOrderRepository {

    private final List<ReservationOrder> store = new ArrayList<>();

    public void save(ReservationOrder order) {
        store.add(order);
    }

    @Override
    public String insert(ReservationOrder order) {
        store.add(order);
        return order.getId();
    }

    @Override
    public Optional<ReservationOrder> findById(String id) {
        return store.stream()
                .filter(order -> order.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<ReservationOrder> findByReservationId(long reservationId) {
        return store.stream()
                .filter(order -> order.getReservationId() == reservationId)
                .findFirst();
    }

    @Override
    public void updatePaymentKey(String id, String paymentKey) {
        for (int i = 0; i < store.size(); i++) {
            ReservationOrder order = store.get(i);
            if (order.getId().equals(id)) {
                store.set(i, order.update(paymentKey));
                return;
            }
        }
    }
}
