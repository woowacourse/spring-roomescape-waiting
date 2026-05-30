package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.theme.application.dto.ThemeResult;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ThemeResult theme,
        ReservationTimeResult time,
        Status status
) {

    public static ReservationResult confirmed(Reservation reservation, ThemeResult themeResult,
                                              ReservationTimeResult timeResult) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getUserName(),
                reservation.getSlot().date(),
                themeResult,
                timeResult,
                Status.CONFIRM
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
                Status.CONFIRM
        );
    }

    public enum Status {
        CONFIRM
    }
}
