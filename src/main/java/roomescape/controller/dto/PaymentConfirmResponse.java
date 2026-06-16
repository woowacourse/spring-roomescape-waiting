package roomescape.controller.dto;

import roomescape.domain.payment.Payment;

public record PaymentConfirmResponse(Long reservationId, String orderId, Long amount) {

    public static PaymentConfirmResponse from(Payment payment) {
        return new PaymentConfirmResponse(payment.getReservationId(), payment.getOrderId(), payment.getAmount());
    }
}
