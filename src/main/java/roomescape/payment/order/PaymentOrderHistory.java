package roomescape.payment.order;

import java.time.LocalDate;
import java.time.LocalTime;

public record PaymentOrderHistory(
        String orderId,
        LocalDate date,
        LocalTime startAt,
        String themeName,
        Long amount,
        String paymentKey,
        PaymentOrderStatus status
) {
}
