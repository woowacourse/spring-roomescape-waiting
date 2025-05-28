package roomescape.reservation.presentation.dto.response;

import java.time.LocalDate;
import roomescape.member.presentation.dto.response.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeResponse;
import roomescape.theme.presentation.dto.response.ThemeResponse;

public record ConfirmedReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ConfirmedReservationResponse of(Reservation reservation, ReservationSlot reservationSlot) {
        return new ConfirmedReservationResponse(reservation.getId(),
                MemberResponse.from(reservationSlot.findReservedMember()), reservationSlot.getDate(),
                ReservationTimeResponse.from(reservationSlot.getTime()), ThemeResponse.from(reservationSlot.getTheme())
        );
    }
}
