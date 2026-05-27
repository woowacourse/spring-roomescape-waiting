package roomescape.reservation.domain;

import roomescape.reservation.exception.ReservationException;

import java.util.List;

import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;

public record Reservations(
        List<Reservation> values
) {

    public void validateNotAlreadyBookedBy(String requestName) {
        boolean alreadyBookedByMyself = values.stream()
                .anyMatch(reservation -> reservation.isOwner(requestName));

        if (alreadyBookedByMyself) {
            throw new ReservationException(RESERVATION_ALREADY_BOOKED);
        }
    }

    public boolean hasReservedByOthers(String name) {
        return values.stream()
                .anyMatch(reservation -> !reservation.isOwner(name) && reservation.isReserved());
    }

}
