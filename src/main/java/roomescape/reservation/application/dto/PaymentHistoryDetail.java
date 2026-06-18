package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record PaymentHistoryDetail(
        Long reservationId,
        String username,
        LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String thumbnailImgUrl,
        Long timeId,
        LocalTime startAt,
        String reservationStatus,
        String orderId,
        Long amount,
        String paymentKey,
        String paymentStatus
) {
}
