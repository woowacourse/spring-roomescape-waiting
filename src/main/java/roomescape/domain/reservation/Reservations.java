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
        if (reservations.stream()
                .filter(reservation -> reservation.getSlot().equals(slot))
                .anyMatch(reservation -> reservation.getName().equals(reservationName))) {
            throw new RoomEscapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private Status decideStatusFor(Slot slot) {
        if (reservations.stream()
                .filter(reservation -> reservation.isSameSlot(slot))
                .anyMatch(reservation -> reservation.getStatus().equals(Status.APPROVED))) {
            return Status.WAITING;
        }
        return Status.APPROVED;
    }

    public List<RankedReservation> rankedReservationsOf(String name) {
        List<Reservation> listByName = findByName(name);

        return listByName.stream()
                .map(this::toRankedReservation)
                .toList();
    }

    private List<Reservation> findByName(String name) {
        return reservations.stream()
                .filter(reservation -> reservation.getName().equals(new ReservationName(name)))
                .toList();
    }

    public List<RankedReservation> allRankedReservationsOf() {
        return reservations.stream()
                .map(this::toRankedReservation)
                .toList();
    }

    private RankedReservation toRankedReservation(Reservation target) {
        List<Reservation> sameSlots = reservations.stream()
                .filter(reservation -> reservation.isSameSlot(target.getSlot()))
                .toList();

        return RankedReservation.decideRankFrom(target, sameSlots);
    }
}
