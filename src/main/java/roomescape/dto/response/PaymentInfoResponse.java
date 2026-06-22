package roomescape.dto.response;

import roomescape.payment.PaymentDetails;
import roomescape.payment.PaymentStatus;

public record PaymentInfoResponse(
        String status,
        String orderId,
        String paymentKey,
        Long amount
) {

    public static PaymentInfoResponse from(PaymentDetails payment) {
        return new PaymentInfoResponse(
                displayStatus(payment.status()),
                payment.orderId(),
                payment.paymentKey(),
                payment.amount()
        );
    }

    private static String displayStatus(PaymentStatus status) {
        if (status == PaymentStatus.DONE) {
            return "확정";
        }
        if (status == PaymentStatus.UNKNOWN) {
            return "확인필요";
        }
        if (status == PaymentStatus.READY) {
            return "결제대기";
        }
        return "실패";
    }
}
