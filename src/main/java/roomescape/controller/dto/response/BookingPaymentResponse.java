package roomescape.controller.dto.response;

import roomescape.domain.PaymentStatus;
import roomescape.service.dto.BookingPaymentInfo;

public record BookingPaymentResponse(
        String orderId,
        Long amount,
        String paymentKey,
        PaymentStatus status,
        String failureCode,
        String failureMessage
) {

    public static BookingPaymentResponse from(BookingPaymentInfo payment) {
        if (payment == null) {
            return null;
        }
        return new BookingPaymentResponse(
                payment.orderId(),
                payment.amount(),
                payment.paymentKey(),
                payment.status(),
                payment.failureCode(),
                payment.failureMessage()
        );
    }
}
