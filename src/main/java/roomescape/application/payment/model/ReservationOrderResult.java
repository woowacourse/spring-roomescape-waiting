package roomescape.application.payment.model;

public record ReservationOrderResult(
        String orderId,
        String orderName,
        Long amount
) {
}
