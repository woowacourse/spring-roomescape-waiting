package roomescape.reservation.presentation.dto;

import roomescape.reservation.application.dto.PaymentReadyResult;

public record PaymentReadyResponse(
        String orderId,
        Long amount
) {

    public static PaymentReadyResponse from(PaymentReadyResult result) {
        if (result == null) {
            return null;
        }

        return new PaymentReadyResponse(
                result.orderId(),
                result.amount()
        );
    }
}
