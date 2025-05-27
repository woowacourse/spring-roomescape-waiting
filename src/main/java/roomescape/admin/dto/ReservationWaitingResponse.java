package roomescape.admin.dto;

import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.theme.dto.ThemeResponse;

public record ReservationWaitingResponse(
        Long id,
        MemberResponse name,
        ThemeResponse theme,
        LocalDate date,
        ReservationTimeResponse startAt
) {

    public static ReservationWaitingResponse from(final Waiting waiting) {
        return new ReservationWaitingResponse(
                waiting.getId(),
                MemberResponse.from(waiting.getMember()),
                ThemeResponse.from(waiting.getTheme()),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getTime())
        );
    }
}
