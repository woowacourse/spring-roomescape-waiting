package roomescape.dto.order.response;

import roomescape.domain.Order;
import roomescape.domain.PaymentStatus;

public record OrderResponse(
        Long id,
        String orderId,
        Long reservationId,
        Long amount,
        PaymentStatus status,
        String paymentKey
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderId().getValue(),
                order.getReservationId(),
                order.getAmount(),
                order.getStatus(),
                order.getPaymentKey());
    }
}
