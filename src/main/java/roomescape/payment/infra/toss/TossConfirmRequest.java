package roomescape.payment.infra.toss;

import roomescape.payment.domain.PaymentConfirmation;

record TossConfirmRequest(
        String paymentKey,
        String orderId,
        long amount
) {
    static TossConfirmRequest from(PaymentConfirmation confirmation) {
        return new TossConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );
    }
}
