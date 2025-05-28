package roomescape.reservation.presentation.dto.response;

import java.time.LocalDate;
import roomescape.member.presentation.dto.response.MemberWebResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeWebResponse;
import roomescape.theme.presentation.dto.response.ThemeWebResponse;

public record ConfirmedReservationWebResponse(
        Long id,
        MemberWebResponse member,
        LocalDate date,
        ReservationTimeWebResponse time,
        ThemeWebResponse theme
) {
    public static ConfirmedReservationWebResponse of(ReservationSlot reservationSlot) {
        Reservation confirmedReservation = reservationSlot.findConfirmedReservation();
        return new ConfirmedReservationWebResponse(confirmedReservation.getId(),
                MemberWebResponse.from(reservationSlot.findReservedMember()), reservationSlot.getDate(),
                ReservationTimeWebResponse.from(reservationSlot.getTime()), ThemeWebResponse.from(reservationSlot.getTheme())
        );
    }
}
