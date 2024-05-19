package roomescape.application.dto;

import java.time.LocalDate;
import roomescape.domain.Waiting;

public record WaitingResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                MemberResponse.from(waiting.getMember()),
                waiting.getDate(),
                TimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }
}
