package roomescape.domain.reservationOrder;

import java.util.Optional;

public interface ReservationOrderRepository {

    String insert(ReservationOrder order);

    Optional<ReservationOrder> findById(String id);

    Optional<ReservationOrder> findByReservationId(long reservationId);

    void updatePaymentKey(String id, String paymentKey);

    void updateStatus(String id, OrderStatus status);
}
