package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

public record AdminReservationWaitingResponse(
        Long reservationId,
        String memberName,
        String themeName,
        LocalDate reservationDate,
        LocalTime reservationStartAt
) {
    public static AdminReservationWaitingResponse from(final Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        return new AdminReservationWaitingResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservationSlot.getTheme().getName(),
                reservationSlot.getDate(),
                reservationSlot.getTime().getStartAt()
        );
    }
}
