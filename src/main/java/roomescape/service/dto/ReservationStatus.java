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
        ReservationSlot slot = reservation.getSlot();
        return new ReservationStatus(
                reservation.getId(),
                reservation.getName(),
                slot.getDate(),
                slot.getTime(),
                slot.getTheme(),
                Status.RESERVED,
                null
        );
    }

    public static ReservationStatus waiting(WaitingWithTurn waitingWithTurn) {
        ReservationWaiting waiting = waitingWithTurn.waiting();
        ReservationSlot slot = waiting.getSlot();
        return new ReservationStatus(
                waiting.getId(),
                waiting.getName(),
                slot.getDate(),
                slot.getTime(),
                slot.getTheme(),
                Status.WAITING,
                waitingWithTurn.turn()
        );
    }
}
