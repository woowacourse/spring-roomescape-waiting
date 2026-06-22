package roomescape.payment.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.payment.domain.PaymentOrderDetail;
import roomescape.payment.domain.PaymentOrderStatus;

public record PaymentOrderHistoryResult(
        Long reservationId,
        String name,
        LocalDate date,
        String themeName,
        LocalTime startAt,
        String orderId,
        long amount,
        PaymentOrderStatus status,
        String paymentKey,
        boolean retryable
) {
    public static PaymentOrderHistoryResult from(PaymentOrderDetail detail) {
        boolean retryable = detail.paymentKey() != null
                && (detail.status() == PaymentOrderStatus.PAYMENT_PENDING
                || detail.status() == PaymentOrderStatus.CONFIRMATION_UNKNOWN);
        return new PaymentOrderHistoryResult(
                detail.reservationId(),
                detail.name(),
                detail.date(),
                detail.themeName(),
                detail.startAt(),
                detail.orderId(),
                detail.amount(),
                detail.status(),
                detail.paymentKey(),
                retryable
        );
    }
}
