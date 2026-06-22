package roomescape.payment.repository;

import roomescape.payment.domain.PaymentOrder;

import java.util.Optional;

public interface PaymentOrderRepository {

    PaymentOrder save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByOrderId(String orderId);

    boolean complete(String orderId, String paymentKey);

    boolean requireConfirmation(String orderId, String paymentKey);

    boolean deleteByOrderId(String orderId);
}
