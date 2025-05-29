package roomescape.dto.waiting;


import java.time.LocalDate;
import roomescape.domain.Waiting;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

public record WaitingResponse(Long id, LocalDate date, ReservationTimeResponse time,
                              ThemeResponse theme, MemberResponse member) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId(), waiting.getDate(), ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme()),
                MemberResponse.from(waiting.getMember()));
    }
}
