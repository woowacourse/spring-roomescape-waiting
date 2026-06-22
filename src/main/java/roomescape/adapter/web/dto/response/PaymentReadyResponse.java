package roomescape.adapter.web.dto.response;

import roomescape.application.dto.result.ReservationOrderResult;

public record PaymentReadyResponse(
        Long reservationId,
        String orderId,
        long amount,
        String orderName,
        String clientKey
) {
    public static PaymentReadyResponse of(ReservationOrderResult result, String clientKey) {
        return new PaymentReadyResponse(
                result.reservationId(), result.orderId(), result.amount(), result.orderName(), clientKey);
    }
}
