package roomescape.reservation.presentation.response;

import java.time.LocalDate;
import roomescape.member.presentation.response.MemberResponse;
import roomescape.reservation.business.domain.Reservation;
import roomescape.theme.presentation.response.ThemeResponse;

public record ReservationResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberResponse member) {

    public static ReservationResponse of(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.of(reservation.getTime()),
                ThemeResponse.of(reservation.getTheme()),
                MemberResponse.of(reservation.getMember())
        );
    }
}
