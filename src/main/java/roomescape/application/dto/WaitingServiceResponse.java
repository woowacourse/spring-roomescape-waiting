package roomescape.application.dto;

import java.time.LocalDate;
import roomescape.domain.entity.Waiting;

public record WaitingServiceResponse(
        long id,
        MemberServiceResponse member,
        ThemeServiceResponse theme,
        LocalDate date,
        TimeServiceResponse time
) {

    public static WaitingServiceResponse from(Waiting waiting) {
        return new WaitingServiceResponse(
                waiting.getId(),
                MemberServiceResponse.from(waiting.getMember()),
                ThemeServiceResponse.from(waiting.getGameSchedule().getTheme()),
                waiting.getGameSchedule().getDate(),
                TimeServiceResponse.from(waiting.getGameSchedule().getTime())
        );
    }
}
