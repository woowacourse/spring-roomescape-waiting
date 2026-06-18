package roomescape.payment.controller.dto;

import roomescape.payment.domain.PaymentOrderDetails;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public record PaymentOrderResponse(
        String orderId,
        long amount,
        String status,
        String name,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Long reservationId,
        String failureCode,
        String failureMessage,
        String createdAt,
        String confirmedAt
) {

    public static PaymentOrderResponse from(PaymentOrderDetails paymentOrder) {
        String confirmedAt = paymentOrder.confirmedAt() == null
                ? null
                : paymentOrder.confirmedAt().toString();

        return new PaymentOrderResponse(
                paymentOrder.orderId(),
                paymentOrder.amount(),
                paymentOrder.status().name(),
                paymentOrder.name(),
                paymentOrder.date().toString(),
                ReservationTimeResponse.from(paymentOrder.time()),
                ThemeResponse.from(paymentOrder.theme()),
                paymentOrder.reservationId(),
                paymentOrder.failureCode(),
                paymentOrder.failureMessage(),
                paymentOrder.createdAt().toString(),
                confirmedAt
        );
    }
}
