package roomescape.controller.client.api.dto.response;

import roomescape.domain.order.OrderType;
import roomescape.pg.PaymentResult;

public record PaymentConfirmResponse(
        String orderId,
        String paymentKey,
        Long amount,
        String paymentStatus,
        OrderType orderType,
        long reservationId
) {
    public static PaymentConfirmResponse from(
            PaymentResult payment,
            OrderType orderType,
            Long targetId
    ) {
        return new PaymentConfirmResponse(
                payment.orderId(),
                payment.paymentKey(),
                payment.approvedAmount(),
                payment.status().name(),
                orderType,
                targetId
        );
    }
}
