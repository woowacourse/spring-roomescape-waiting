package roomescape.payment.domain;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository {

    PaymentOrder insert(PaymentOrder order);

    Optional<PaymentOrder> findByOrderId(String orderId);

    Optional<PaymentOrder> findByOrderIdForUpdate(String orderId);

    List<PaymentOrder> findAllByReservationIdIn(List<Long> reservationIds);

    PaymentOrder update(PaymentOrder order);

    int deleteByOrderId(String orderId);

    int deleteByReservationId(Long reservationId);
}
