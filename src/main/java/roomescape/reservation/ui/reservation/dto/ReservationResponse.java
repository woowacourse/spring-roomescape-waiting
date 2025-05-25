package roomescape.reservation.ui.reservation.dto;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.reservation.application.reservation.dto.ReservationInfo;
import roomescape.reservation.ui.theme.dto.ThemeResponse;
import roomescape.reservation.ui.timeslot.dto.TimeSlotResponse;

public record ReservationResponse(
        long id,
        MemberResponse member,
        LocalDate date,
        TimeSlotResponse time,
        ThemeResponse theme
) {

    public ReservationResponse(final ReservationInfo reservationInfo) {
        this(reservationInfo.id(),
                new MemberResponse(reservationInfo.member()),
                reservationInfo.date(),
                new TimeSlotResponse(reservationInfo.time()),
                new ThemeResponse(reservationInfo.theme())
        );
    }
}
