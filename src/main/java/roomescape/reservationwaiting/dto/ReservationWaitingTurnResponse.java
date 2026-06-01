package roomescape.reservationwaiting.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public record ReservationWaitingTurnResponse(Long id, String name, Long turn,
                                             LocalDate date, LocalTime startAt, String themeName) {

    public static ReservationWaitingTurnResponse from(ReservationWaiting reservationWaiting, Long turn) {
        return new ReservationWaitingTurnResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                turn,
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getStartAt(),
                reservationWaiting.getTheme().getName()
        );
    }
}
