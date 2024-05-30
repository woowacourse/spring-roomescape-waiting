package roomescape.dto.response;

import roomescape.domain.Waiting;

import java.time.LocalDate;

public record WaitingResponse(Long id, MemberResponse member, LocalDate date,
                              TimeSlotResponse time, ThemeResponse theme) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                MemberResponse.from(waiting.getMember()),
                waiting.getDate(),
                TimeSlotResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }
}
