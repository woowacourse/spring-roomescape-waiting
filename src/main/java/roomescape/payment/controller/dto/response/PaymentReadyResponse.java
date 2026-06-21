package roomescape.payment.controller.dto.response;

import roomescape.payment.domain.PaymentOrder;
import roomescape.reservation.domain.Reservation;

public record PaymentReadyResponse(
        Long reservationId,
        String orderId,
        int amount,
        String orderName,
        String customerName,
        String customerEmail
) {
    public static PaymentReadyResponse of(final Reservation reservation, final PaymentOrder paymentOrder) {
        return new PaymentReadyResponse(
                reservation.getId(),
                paymentOrder.getOrderId(),
                paymentOrder.getAmount(),
                "%s %s %s".formatted(
                        reservation.getTheme().getName(),
                        reservation.getDate(),
                        reservation.getTime().getStartAt()
                ),
                reservation.getCustomerName(),
                reservation.getCustomerEmail()
        );
    }
}
