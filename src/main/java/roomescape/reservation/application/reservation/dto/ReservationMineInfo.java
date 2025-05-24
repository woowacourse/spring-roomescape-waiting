package roomescape.reservation.application.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.application.theme.dto.ThemeInfo;
import roomescape.reservation.application.time.dto.ReservationTimeInfo;
import roomescape.reservation.application.waiting.dto.ReservationWaitingInfo;

public record ReservationMineInfo(
        long id,
        LocalDate date,
        ReservationTimeInfo timeInfo,
        ThemeInfo themeInfo,
        String status
) {

    public ReservationMineInfo(final ReservationInfo reservationInfo) {
        this(
                reservationInfo.id(),
                reservationInfo.date(),
                reservationInfo.time(),
                reservationInfo.theme(),
                reservationInfo.status()
        );
    }

    public ReservationMineInfo(final ReservationWaitingInfo reservationWaitingInfo) {
        this(
                reservationWaitingInfo.id(),
                reservationWaitingInfo.reservation().date(),
                reservationWaitingInfo.reservation().time(),
                reservationWaitingInfo.reservation().theme(),
                reservationWaitingInfo.status()
        );    }
}
