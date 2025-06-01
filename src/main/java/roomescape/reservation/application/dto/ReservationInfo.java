package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.member.application.dto.MemberInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.presentation.util.ReservationStatusDisplay;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.timeslot.application.dto.TimeSlotInfo;

public record ReservationInfo(
        long id,
        MemberInfo member,
        LocalDate date,
        TimeSlotInfo time,
        ThemeInfo theme,
        String status
) {

    public ReservationInfo(final Reservation reservation) {
        this(
                reservation.id(),
                new MemberInfo(reservation.member()),
                reservation.date(),
                new TimeSlotInfo(reservation.time()),
                new ThemeInfo(reservation.theme()),
                ReservationStatusDisplay.display(ReservationStatus.BOOKED)
        );
    }
}
