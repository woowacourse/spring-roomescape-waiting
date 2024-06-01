package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Waiting;

public record WaitingResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ThemeResponse theme,
        TimeResponse time
) {

    public WaitingResponse(final Waiting waiting) {
        this(
                waiting.getId(),
                new MemberResponse(waiting.getMember()),
                waiting.getDate(),
                new ThemeResponse(waiting.getTheme()),
                new TimeResponse(waiting.getReservationTime())
        );

    }
}
