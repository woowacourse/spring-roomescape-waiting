package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.payment.PaymentStatus;

public record ReservationDetail(
        Long reservationId,
        String username,
        LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String thumbnailImgUrl,
        Long timeId,
        LocalTime startAt,
        PaymentStatus paymentStatus
) {
}
