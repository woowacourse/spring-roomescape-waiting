package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record AdminReservationWaitingResponse(
        Long reservationId,
        String memberName,
        String themeName,
        LocalDate reservationDate,
        LocalTime reservationStartAt
) {
    public static AdminReservationWaitingResponse from(final Reservation reservation) {
        return new AdminReservationWaitingResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
