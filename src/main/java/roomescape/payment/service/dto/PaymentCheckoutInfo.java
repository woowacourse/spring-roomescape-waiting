package roomescape.payment.service.dto;

public record PaymentCheckoutInfo(
        Long reservationId,
        String reservationName,
        String orderId,
        Long amount
) {
}
