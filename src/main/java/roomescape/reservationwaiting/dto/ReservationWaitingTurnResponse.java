package roomescape.reservationwaiting.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public record ReservationWaitingTurnResponse(Long id, String name, Long reservationId, Long turn,
                                             LocalDate date, LocalTime startAt, String themeName) {

    public static ReservationWaitingTurnResponse from(ReservationWaiting reservationWaiting, Long turn) {
        return new ReservationWaitingTurnResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getReservation().getId(),
                turn,
                reservationWaiting.getReservation().getDate(),
                reservationWaiting.getReservation().getTime().getStartAt(),
                reservationWaiting.getReservation().getTheme().getName()
        );
    }
}