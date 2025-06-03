package roomescape.reservation.waiting.dto;

import roomescape.member.dto.MemberResponse;
import roomescape.reservation.waiting.domain.Waiting;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.dto.ReservationTimeResponse;

import java.time.LocalDate;

public record WaitingResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public WaitingResponse(final Waiting waiting) {
        this(
                waiting.getId(),
                new MemberResponse(waiting.getMember()),
                waiting.getDate(),
                new ReservationTimeResponse(waiting.getTime()),
                new ThemeResponse(waiting.getTheme())
        );
    }
}

