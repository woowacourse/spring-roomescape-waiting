package roomescape.payment.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.payment.application.dto.PaymentOrderHistoryResult;
import roomescape.payment.domain.PaymentOrderStatus;

public record PaymentOrderHistoryResponse(
        Long reservationId,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        String themeName,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        String orderId,
        long amount,
        PaymentOrderStatus status,
        String paymentKey,
        boolean retryable
) {
    public static PaymentOrderHistoryResponse from(PaymentOrderHistoryResult result) {
        return new PaymentOrderHistoryResponse(
                result.reservationId(),
                result.name(),
                result.date(),
                result.themeName(),
                result.startAt(),
                result.orderId(),
                result.amount(),
                result.status(),
                result.paymentKey(),
                result.retryable()
        );
    }
}
