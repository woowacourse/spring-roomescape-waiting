package roomescape.payment;

import roomescape.payment.dto.PaymentResult;

public interface PaymentGateway {

    PaymentResult confirm(String paymentKey, String orderId, long amount);

    void cancel(String paymentKey, String cancelReason);
}
