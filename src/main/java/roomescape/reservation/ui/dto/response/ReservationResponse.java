package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.ui.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        IdName member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                IdName.from(reservation.getMember()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()));
    }
}
