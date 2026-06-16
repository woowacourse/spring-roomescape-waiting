package roomescape.reservation.presentation.dto;

public record PaymentConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        Long approvedAmount
) {

    public static PaymentConfirmResponse of(String paymentKey, String orderId, String status, Long approvedAmount) {
        return new PaymentConfirmResponse(
                paymentKey,
                orderId,
                status,
                approvedAmount
        );
    }
}
