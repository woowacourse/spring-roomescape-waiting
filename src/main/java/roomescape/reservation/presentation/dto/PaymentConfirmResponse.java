package roomescape.reservation.presentation.dto;

import roomescape.reservation.application.port.out.payment.PaymentResult;

public record PaymentConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        Long approvedAmount
) {

    public static PaymentConfirmResponse of(PaymentResult result) {
        return new PaymentConfirmResponse(
                result.paymentKey(),
                result.orderId(),
                result.status().name(),
                result.approvedAmount()
        );
    }
}
