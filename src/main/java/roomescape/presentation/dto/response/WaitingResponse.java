package roomescape.presentation.dto.response;

import roomescape.domain.Waiting;

import java.time.LocalDate;

public record WaitingResponse(
        Long id,
        LocalDate date,
        MemberResponse member,
        ThemeResponse theme,
        ReservationTimeResponse time
) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getDate(),
                MemberResponse.from(waiting.getMember()),
                ThemeResponse.from(waiting.getTheme()),
                ReservationTimeResponse.from(waiting.getTime())
        );
    }
}
