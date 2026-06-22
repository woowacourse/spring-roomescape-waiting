package roomescape.payment.infrastructure.dto;

import roomescape.payment.domain.PaymentConfirmation;

public record TossConfirmRequest(String paymentKey, String orderId, long amount) {

    public static TossConfirmRequest from(PaymentConfirmation confirmation) {
        return new TossConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );
    }
}
