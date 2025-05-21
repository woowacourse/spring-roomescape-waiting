package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.member.application.dto.MemberResponse;
import roomescape.reservation.domain.Waiting;
import roomescape.theme.application.dto.ThemeResponse;

public record WaitingResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberResponse member
) {

    public static WaitingResponse from(final Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme()),
                MemberResponse.from(waiting.getMember())
        );
    }
}
