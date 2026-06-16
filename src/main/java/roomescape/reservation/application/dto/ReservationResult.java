package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.PaymentOrder;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.theme.application.dto.ThemeResult;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ThemeResult theme,
        ReservationTimeResult time,
        String status,
        PaymentReadyResult payment
) {

    public static ReservationResult confirmed(Reservation reservation, ThemeResult themeResult,
                                              ReservationTimeResult timeResult) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getUserName(),
                reservation.getSlot().date(),
                themeResult,
                timeResult,
                reservation.getStatus().name(),
                null
        );
    }

    public static ReservationResult paymentPending(Reservation reservation, ThemeResult themeResult,
                                                   ReservationTimeResult timeResult, PaymentOrder paymentOrder) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getUserName(),
                reservation.getSlot().date(),
                themeResult,
                timeResult,
                reservation.getStatus().name(),
                PaymentReadyResult.from(paymentOrder)
        );
    }

    public static ReservationResult from(ReservationDetail reservationDetail) {
        return new ReservationResult(
                reservationDetail.reservationId(),
                reservationDetail.username(),
                reservationDetail.date(),
                ThemeResult.from(
                        reservationDetail.themeId(),
                        reservationDetail.themeName(),
                        reservationDetail.themeDescription(),
                        reservationDetail.thumbnailImgUrl()
                ),
                ReservationTimeResult.from(
                        reservationDetail.timeId(),
                        reservationDetail.startAt()
                ),
                reservationDetail.status().name(),
                PaymentReadyResult.from(reservationDetail.orderId(), reservationDetail.amount())
        );
    }
}
