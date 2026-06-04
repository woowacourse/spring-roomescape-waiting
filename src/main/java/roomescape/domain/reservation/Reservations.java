package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.List;

public class Reservations {
    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Reservation reserve(ReservationName reservationName, Slot foundSlot, LocalDateTime now) {
        validateHasName(reservationName, foundSlot);

        Status status = getStatus();

        return Reservation.create(reservationName, foundSlot, status, now);
    }

    private void validateHasName(ReservationName reservationName, Slot slot) {
        if (reservations.stream()
                .filter(reservation -> reservation.getSlot().equals(slot))
                .anyMatch(reservation -> reservation.getName().equals(reservationName))) {
            throw new RoomEscapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private Status getStatus() {
        if (reservations.stream()
                .anyMatch(reservation -> reservation.getStatus().equals(Status.APPROVED))) {
            return Status.WAITING;
        }
        return Status.APPROVED;
    }
}
