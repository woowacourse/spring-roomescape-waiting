package roomescape.reservation.application.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.application.theme.dto.ThemeInfo;
import roomescape.reservation.application.timeslot.dto.TimeSlotInfo;
import roomescape.reservation.application.waiting.dto.WaitingInfo;

public record ReservationMineInfo(
        long id,
        LocalDate date,
        TimeSlotInfo timeInfo,
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

    public ReservationMineInfo(final WaitingInfo waitingInfo) {
        this(
                waitingInfo.id(),
                waitingInfo.date(),
                waitingInfo.time(),
                waitingInfo.theme(),
                waitingInfo.status()
        );    }
}
