package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Reservations {
    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = Objects.requireNonNull(reservations);
    }

    public Reservation reserve(ReservationName reservationName, Slot foundSlot, LocalDateTime now) {
        validateNoDuplicate(reservationName, foundSlot);

        Status status = decideStatusFor(foundSlot);

        return Reservation.create(reservationName, foundSlot, status, now);
    }

    private void validateNoDuplicate(ReservationName reservationName, Slot slot) {
        if (hasNameAndSlot(reservationName, slot)) {
            throw new RoomEscapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private boolean hasNameAndSlot(ReservationName reservationName, Slot slot) {
        return reservations.stream()
                .filter(reservation -> reservation.hasSameSlot(slot))
                .anyMatch(reservation -> reservation.hasSameName(reservationName));
    }

    private Status decideStatusFor(Slot slot) {
        if (reservations.stream()
                .filter(reservation -> reservation.hasSameSlot(slot))
                .anyMatch(reservation -> reservation.isApproved())) {
            return Status.WAITING;
        }
        return Status.APPROVED;
    }

    private List<Reservation> findByName(String name) {
        return reservations.stream()
                .filter(reservation -> reservation.hasSameName(new ReservationName(name)))
                .toList();
    }

    public List<RankedReservation> rankedReservationsOf(String name) {
        List<Reservation> target = reservations;
        if (name != null) {
            target = findByName(name);
        }
        return target.stream()
                .map(this::toRankedReservation)
                .toList();
    }

    public List<RankedReservation> rankedReservationsOf() {
        return reservations.stream()
                .map(this::toRankedReservation)
                .toList();
    }

    private RankedReservation toRankedReservation(Reservation target) {
        List<Reservation> sameSlots = reservations.stream()
                .filter(reservation -> reservation.hasSameSlot(target))
                .toList();

        return RankedReservation.decideRankFrom(target, sameSlots);
    }
}
