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
                waiting.getDetail().getDate(),
                MemberResponse.from(waiting.getMember()),
                ReservationTimeResponse.from(waiting.getDetail().getTime()),
                ThemeResponse.from(waiting.getDetail().getTheme())
        );
    }
}
