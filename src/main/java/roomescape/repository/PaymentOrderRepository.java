package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.payment.PaymentOrder;

public interface PaymentOrderRepository {

    Long save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByOrderId(String orderId);

    List<PaymentOrder> findAllByReservationIds(List<Long> reservationIds);

    int updatePaymentKey(String orderId, String paymentKey);

    int deleteByOrderId(String orderId);
}
