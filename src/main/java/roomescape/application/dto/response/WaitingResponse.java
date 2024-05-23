package roomescape.application.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Waiting;

public record WaitingResponse(
        Long id,
        LocalDate date,
        MemberResponse member,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getDate(),
                MemberResponse.from(waiting.getMember()),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }
}
