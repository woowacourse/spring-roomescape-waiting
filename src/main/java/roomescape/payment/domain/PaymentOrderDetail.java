package roomescape.payment.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public record PaymentOrderDetail(
        Long reservationId,
        String name,
        LocalDate date,
        String themeName,
        LocalTime startAt,
        String orderId,
        long amount,
        PaymentOrderStatus status,
        String paymentKey
) {
}
