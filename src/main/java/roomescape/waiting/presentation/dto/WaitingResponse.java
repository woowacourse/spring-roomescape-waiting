package roomescape.waiting.presentation.dto;

import java.time.LocalDate;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.theme.presentation.dto.ThemeResponse;
import roomescape.timeslot.presentation.dto.TimeSlotResponse;
import roomescape.waiting.application.dto.WaitingInfo;

public record WaitingResponse(
        long id,
        LocalDate date,
        TimeSlotResponse time,
        ThemeResponse theme,
        MemberResponse member
) {

    public WaitingResponse(final WaitingInfo waitingInfo) {
        this(waitingInfo.id(),
                waitingInfo.reservationInfo().date(),
                new TimeSlotResponse(waitingInfo.reservationInfo().time()),
                new ThemeResponse(waitingInfo.reservationInfo().theme()),
                new MemberResponse(waitingInfo.reservationInfo().member())
        );
    }
}
