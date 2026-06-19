package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.theme.application.dto.ThemeResult;

public record PaymentHistoryResult(
        Long reservationId,
        String username,
        LocalDate date,
        ThemeResult theme,
        ReservationTimeResult time,
        String reservationStatus,
        String orderId,
        Long amount,
        String paymentKey,
        String paymentStatus
) {

    public static PaymentHistoryResult from(PaymentHistoryDetail detail) {
        return new PaymentHistoryResult(
                detail.reservationId(),
                detail.username(),
                detail.date(),
                ThemeResult.from(
                        detail.themeId(),
                        detail.themeName(),
                        detail.themeDescription(),
                        detail.thumbnailImgUrl()
                ),
                ReservationTimeResult.from(
                        detail.timeId(),
                        detail.startAt()
                ),
                detail.reservationStatus(),
                detail.orderId(),
                detail.amount(),
                detail.paymentKey(),
                detail.paymentStatus()
        );
    }
}
