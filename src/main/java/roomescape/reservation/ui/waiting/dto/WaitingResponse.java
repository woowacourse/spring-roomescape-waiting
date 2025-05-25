package roomescape.reservation.ui.waiting.dto;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.reservation.application.waiting.dto.WaitingInfo;
import roomescape.reservation.ui.theme.dto.ThemeResponse;
import roomescape.reservation.ui.timeslot.dto.TimeSlotResponse;

public record WaitingResponse(
        long id,
        LocalDate date,
        TimeSlotResponse time,
        ThemeResponse theme,
        MemberResponse member
) {

    public WaitingResponse(final WaitingInfo waitingInfo) {
        this(waitingInfo.id(),
                waitingInfo.date(),
                new TimeSlotResponse(waitingInfo.time()),
                new ThemeResponse(waitingInfo.theme()),
                new MemberResponse(waitingInfo.member())
        );
    }
}
