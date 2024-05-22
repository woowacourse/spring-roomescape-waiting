package roomescape.service.dto.response;

import java.time.LocalDate;

public record WaitingResponse(
        long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

}
