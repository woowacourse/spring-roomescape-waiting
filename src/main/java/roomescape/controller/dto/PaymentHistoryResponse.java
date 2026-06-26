package roomescape.controller.dto;

import java.time.LocalDate;
import roomescape.domain.PaymentOrderStatus;
import roomescape.service.dto.PaymentHistory;

public record PaymentHistoryResponse(
        String orderId,
        String status,
        String statusLabel,
        Long amount,
        String paymentKey,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme
) {

    public static PaymentHistoryResponse from(PaymentHistory history) {
        return new PaymentHistoryResponse(
                history.orderId(),
                history.status().name(),
                label(history.status()),
                history.amount(),
                history.paymentKey(),
                history.date(),
                history.timeSlot() == null ? null : TimeResponse.from(history.timeSlot()),
                history.theme() == null ? null : ThemeResponse.from(history.theme())
        );
    }

    private static String label(PaymentOrderStatus status) {
        return switch (status) {
            case PENDING -> "결제 대기";
            case CONFIRMED -> "확정";
            case FAILED -> "실패";
            case UNKNOWN -> "확인 필요";
        };
    }
}
