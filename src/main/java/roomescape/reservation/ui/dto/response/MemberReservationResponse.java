package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

public record MemberReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        Long rank
) {

    public static MemberReservationResponse from(final Reservation reservation, final Long rank) {
        final ReservationSlot reservationSlot = reservation.getReservationSlot();
        return new MemberReservationResponse(
                reservation.getId(),
                reservationSlot.getTheme().getName(),
                reservationSlot.getDate(),
                reservationSlot.getTime().getStartAt(),
                rank
        );
    }
}
