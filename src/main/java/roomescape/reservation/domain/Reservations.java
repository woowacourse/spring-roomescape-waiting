package roomescape.reservation.domain;

import roomescape.reservation.exception.ReservationException;
import roomescape.slot.domain.ReservationSlot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;

public record Reservations(
        List<Reservation> values
) {

    public Reservations {
        values = new ArrayList<>(values);
    }

    public Reservation reserve(String requesterName, ReservationSlot slot, LocalDateTime reservedAt) {
        validateNotAlreadyBookedBy(requesterName);
        if (hasReservedByOthers(requesterName)) {
            return register(Reservation.wait(requesterName, slot, reservedAt));
        }

        return register(Reservation.reserve(requesterName, slot, reservedAt));
    }

    public void validateNotAlreadyBookedBy(String requestName) {
        boolean alreadyBookedByMyself = values.stream()
                .anyMatch(reservation -> reservation.isOwner(requestName));

        if (alreadyBookedByMyself) {
            throw new ReservationException(RESERVATION_ALREADY_BOOKED);
        }
    }

    public Optional<Reservation> findPromoteWaiting() {
        return values.stream()
                .filter(Reservation::isWaiting)
                .min(Comparator.comparing(Reservation::getReservedAt).thenComparing(Reservation::getId));
    }

    public boolean hasReservedByOthers(String name) {
        return values.stream()
                .anyMatch(reservation -> !reservation.isOwner(name) && reservation.isReserved());
    }

    private Reservation register(Reservation reservation) {
        values.add(reservation);
        return reservation;
    }

}
