package roomescape.controller.dto.payment;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.service.dto.PaymentOrderHistory;

public record PaymentOrderHistoryResponse(
        String orderId,
        Long reservationId,
        String status,
        String statusLabel,
        String paymentKey,
        int amount,
        String failureCode,
        String failureMessage,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl
) {

    public static PaymentOrderHistoryResponse from(PaymentOrderHistory history) {
        return new PaymentOrderHistoryResponse(
                history.orderId(),
                history.reservationId(),
                history.status().name(),
                statusLabel(history),
                history.paymentKey(),
                history.amount(),
                history.failureCode(),
                history.failureMessage(),
                history.date(),
                history.time(),
                history.themeName(),
                history.themeDescription(),
                history.themeThumbnailUrl()
        );
    }

    private static String statusLabel(PaymentOrderHistory history) {
        return switch (history.status()) {
            case PENDING -> "결제 대기";
            case CONFIRMED -> "예약 확정";
            case FAILED -> "결제 실패";
            case CONFIRM_UNKNOWN -> "확인 필요";
        };
    }
}
