package roomescape.booking.waiting.dto;

import roomescape.booking.schedule.dto.ScheduleResponse;
import roomescape.booking.waiting.Waiting;
import roomescape.member.dto.MemberResponse;

public record WaitingResponse(
        Long id,
        ScheduleResponse schedule,
        MemberResponse member,
        Long rank
) {

    public static WaitingResponse of(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                ScheduleResponse.of(waiting.getSchedule()),
                MemberResponse.from(waiting.getMember()),
                waiting.getRank());
    }
}
