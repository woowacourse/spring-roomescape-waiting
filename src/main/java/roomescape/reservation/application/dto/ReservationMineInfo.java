package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.timeslot.application.dto.TimeSlotInfo;
import roomescape.waiting.application.dto.WaitingInfo;

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
                waitingInfo.reservationInfo().date(),
                waitingInfo.reservationInfo().time(),
                waitingInfo.reservationInfo().theme(),
                waitingInfo.status()
        );    }
}
