package roomescape.controller.dto.response;

import roomescape.service.dto.ReservationPayment;

public record PaymentOrderResponse(
        String status,
        String statusLabel,
        String orderId,
        String paymentKey,
        Long amount
) {

    public static PaymentOrderResponse from(ReservationPayment payment) {
        if (payment == null) {
            return null;
        }
        return new PaymentOrderResponse(
                payment.status().name(),
                label(payment.status().name()),
                payment.orderId(),
                payment.paymentKey(),
                payment.amount()
        );
    }

    private static String label(String status) {
        return switch (status) {
            case "PENDING" -> "결제 대기";
            case "CONFIRMED" -> "확정";
            case "FAILED" -> "실패";
            case "UNKNOWN" -> "확인 필요";
            default -> "확인 필요";
        };
    }
}
