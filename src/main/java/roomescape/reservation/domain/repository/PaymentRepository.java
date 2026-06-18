package roomescape.reservation.domain.repository;

import java.util.Optional;
import roomescape.reservation.domain.Payment;

public interface PaymentRepository {
    Payment save(Payment paymentOrder);

    Optional<Payment> findByOrderId(String orderId);

    Integer confirm(Payment paymentOrder);

    Integer deletePendingByOrderId(String orderId);

    Integer deleteByReservationId(Long reservationId);
}
