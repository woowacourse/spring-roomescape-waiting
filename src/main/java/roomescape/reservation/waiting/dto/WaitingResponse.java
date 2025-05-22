package roomescape.reservation.waiting.dto;

import roomescape.member.dto.MemberResponse;
import roomescape.reservation.waiting.Waiting;
import roomescape.schedule.dto.ScheduleResponse;

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
