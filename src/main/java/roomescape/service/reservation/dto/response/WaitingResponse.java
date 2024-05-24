package roomescape.service.reservation.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Waiting;
import roomescape.service.member.dto.MemberResponse;

public record WaitingResponse(Long id, String name,
                              LocalDate date, MemberResponse member,
                              ReservationTimeResponse time, ThemeResponse theme) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                MemberResponse.from(waiting.getMember()),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }
}
