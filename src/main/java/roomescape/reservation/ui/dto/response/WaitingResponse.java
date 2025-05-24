package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.reservation.domain.Waiting;
import roomescape.theme.ui.dto.ThemeResponse;

public record WaitingResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        IdName member,
        LocalDateTime createdAt
) {

    public static WaitingResponse from(final Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme()),
                IdName.from(waiting.getMember()),
                waiting.getCreatedAt()
        );
    }
}
