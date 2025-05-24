package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.ui.dto.ThemeResponse;

public record AdminReservationResponse(
        Long id,
        IdName member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Long rank
) {

    public static AdminReservationResponse from(final Reservation reservation, final Long rank) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        return new AdminReservationResponse(
                reservation.getId(),
                IdName.from(reservation.getMember()),
                reservationSlot.getDate(),
                ReservationTimeResponse.from(reservationSlot.getTime()),
                ThemeResponse.from(reservationSlot.getTheme()),
                rank
        );
    }
}
