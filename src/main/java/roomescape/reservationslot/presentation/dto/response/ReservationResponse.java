package roomescape.reservationslot.presentation.dto.response;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.member.presentation.dto.response.MemberResponse;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.presentation.dto.response.ThemeResponse;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationResponse of(ReservationSlot reservationSlot, ReservationTime reservationTime, Theme theme,
                                         Member member) {
        return new ReservationResponse(reservationSlot.getId(), MemberResponse.from(member), reservationSlot.getDate(),
                ReservationTimeResponse.from(reservationTime), ThemeResponse.from(theme)
        );
    }
}
