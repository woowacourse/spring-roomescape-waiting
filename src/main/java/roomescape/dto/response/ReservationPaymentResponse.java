package roomescape.dto.response;

public record ReservationPaymentResponse(
        Long reservationId,
        String orderId,
        long amount
) {
}
