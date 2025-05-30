package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.ReservationWaiting;

public record AdminReservationWaitingServiceResponse(
        Long id,
        String waitingName,
        String themeName,
        LocalDate date,
        LocalTime startAt
) {

    public static AdminReservationWaitingServiceResponse from(ReservationWaiting reservationWaiting) {
        return new AdminReservationWaitingServiceResponse(
                reservationWaiting.getId(),
                reservationWaiting.getMember().getName(),
                reservationWaiting.getTheme().getName(),
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getStartAt()
        );
    }
}
