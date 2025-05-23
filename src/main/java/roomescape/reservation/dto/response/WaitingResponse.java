package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.member.dto.response.MemberResponse;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.response.ThemeResponse;

public record WaitingResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String turn
) {
    public static WaitingResponse of(Waiting waiting, ReservationTime reservationTime, Theme theme,
                                     Member member, int order) {
        return new WaitingResponse(waiting.getId(), MemberResponse.from(member),
                waiting.getInfo().getDate(),
                ReservationTimeResponse.from(reservationTime), ThemeResponse.from(theme),
                ReservationStatus.WAITING.getName() + order + "번째"
        );
    }

}

