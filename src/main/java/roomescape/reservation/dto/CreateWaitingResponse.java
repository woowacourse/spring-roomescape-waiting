package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Waiting;
import roomescape.theme.dto.ThemeResponse;

public record CreateWaitingResponse(
        Long id,
        LocalDate date,
        MemberResponse member,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static CreateWaitingResponse from(final Waiting waiting) {
        return new CreateWaitingResponse(
                waiting.getId(),
                waiting.getDate(),
                MemberResponse.from(waiting.getMember()),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }

}
