package roomescape.service.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.payment.PaymentOrderStatus;

public record PaymentOrderHistory(
        String orderId,
        Long reservationId,
        PaymentOrderStatus status,
        String paymentKey,
        int amount,
        String failureCode,
        String failureMessage,
        LocalDate date,
        LocalTime time,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl
) {
}
