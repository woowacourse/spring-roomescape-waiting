package roomescape.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.payment.order.PaymentOrderHistory;

public record OrderHistoryResponse(
        String orderId,
        String themeName,
        LocalDate date,
        LocalTime startAt,
        Long amount,
        String paymentKey,
        String paymentStatus
) {

    public static OrderHistoryResponse from(PaymentOrderHistory history) {
        return new OrderHistoryResponse(
                history.orderId(),
                history.themeName(),
                history.date(),
                history.startAt(),
                history.amount(),
                history.paymentKey(),
                history.status().name()
        );
    }
}
