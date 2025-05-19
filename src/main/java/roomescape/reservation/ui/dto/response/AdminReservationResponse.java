package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.ui.dto.ThemeResponse;

public record AdminReservationResponse(
        Long id,
        IdName member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status
) {

    public static AdminReservationResponse from(final Reservation reservation) {
        return new AdminReservationResponse(
                reservation.getId(),
                IdName.from(reservation.getMember()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getState().getDescription()
        );
    }
}
