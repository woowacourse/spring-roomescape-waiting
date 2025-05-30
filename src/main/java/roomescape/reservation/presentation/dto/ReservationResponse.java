package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.theme.presentation.dto.ThemeResponse;
import roomescape.timeslot.presentation.dto.TimeSlotResponse;

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
