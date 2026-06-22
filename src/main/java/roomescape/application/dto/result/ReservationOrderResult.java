package roomescape.application.dto.result;

public record ReservationOrderResult(
        Long reservationId,
        String orderId,
        long amount,
        String orderName
) {
}
