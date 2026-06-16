package roomescape.domain.reservationOrder;

import java.util.Optional;

public interface ReservationOrderRepository {

    String insert(ReservationOrder order);

    Optional<ReservationOrder> findById(String id);

    void updatePaymentKey(String id, String paymentKey);
}
