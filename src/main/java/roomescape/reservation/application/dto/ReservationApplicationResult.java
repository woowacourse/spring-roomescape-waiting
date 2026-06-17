package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.theme.application.dto.ThemeResult;

public record ReservationApplicationResult(
        Long id,
        String name,
        LocalDate date,
        ThemeResult theme,
        ReservationTimeResult time,
        Status status,
        roomescape.payment.PaymentStatus paymentStatus,
        Long rank
) {

    public static ReservationApplicationResult confirmed(Reservation reservation, ThemeResult themeResult,
                                                         ReservationTimeResult timeResult) {
        return new ReservationApplicationResult(
                reservation.getId(),
                reservation.getMemberName().name(),
                reservation.getSlot().date(),
                themeResult,
                timeResult,
                Status.CONFIRM,
                reservation.getPaymentStatus(),
                null
        );
    }

    public static ReservationApplicationResult waiting(Waiting waiting, ThemeResult themeResult,
                                                       ReservationTimeResult timeResult,
                                                       Long rank) {
        return new ReservationApplicationResult(
                waiting.getId(),
                waiting.getMemberName().name(),
                waiting.getSlot().date(),
                themeResult,
                timeResult,
                Status.WAITING,
                null,
                rank
        );
    }

    public static ReservationApplicationResult from(ReservationDetail reservationDetail) {
        return new ReservationApplicationResult(
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
                Status.CONFIRM,
                reservationDetail.paymentStatus(),
                null
        );
    }

    public static ReservationApplicationResult from(WaitingDetail waitingDetail) {
        return new ReservationApplicationResult(
                waitingDetail.waitingId(),
                waitingDetail.username(),
                waitingDetail.date(),
                ThemeResult.from(
                        waitingDetail.themeId(),
                        waitingDetail.themeName(),
                        waitingDetail.themeDescription(),
                        waitingDetail.thumbnailImgUrl()
                ),
                ReservationTimeResult.from(
                        waitingDetail.timeId(),
                        waitingDetail.startAt()
                ),
                Status.WAITING,
                null,
                waitingDetail.rank()
        );
    }

    public enum Status {
        CONFIRM, WAITING
    }
}
