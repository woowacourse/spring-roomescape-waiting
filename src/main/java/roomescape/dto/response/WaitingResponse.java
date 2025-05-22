package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Waiting;

public record WaitingResponse(
        Long id,
        LocalDate date,
        ThemeProfileResponse themeResponse,
        ReservationTimeResponse timeResponse,
        MemberProfileResponse memberProfileResponse
) {

    public WaitingResponse(Waiting waiting) {
        this(waiting.getId(), waiting.getDate(),
                new ThemeProfileResponse(waiting.getTheme()),
                new ReservationTimeResponse(waiting.getTime()),
                new MemberProfileResponse(waiting.getMember()));
    }
}
