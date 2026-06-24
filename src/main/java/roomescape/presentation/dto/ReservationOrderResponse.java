package roomescape.presentation.dto;

import roomescape.application.payment.model.ReservationOrderResult;

public record ReservationOrderResponse(
        String orderId,
        String orderName,
        Long amount
) {

    public static ReservationOrderResponse from(ReservationOrderResult result) {
        return new ReservationOrderResponse(
                result.orderId(),
                result.orderName(),
                result.amount()
        );
    }
}
