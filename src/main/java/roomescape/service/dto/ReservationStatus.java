package roomescape.service.dto;

import roomescape.domain.*;

import java.time.LocalDate;

public record ReservationStatus(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        Status status,
        Long turn
) {
    public static ReservationStatus reserved(Reservation reservation) {
        return new ReservationStatus(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                Status.RESERVED,
                null
        );
    }

    public static ReservationStatus waiting(WaitingWithTurn waitingWithTurn) {
        ReservationWaiting waiting = waitingWithTurn.waiting();
        return new ReservationStatus(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme(),
                Status.WAITING,
                waitingWithTurn.turn()
        );
    }
}
