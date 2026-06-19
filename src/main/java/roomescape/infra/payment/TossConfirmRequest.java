package roomescape.infra.payment;

import roomescape.payment.PaymentConfirmation;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {

    public static TossConfirmRequest from(PaymentConfirmation confirmation) {
        return new TossConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );
    }
}
