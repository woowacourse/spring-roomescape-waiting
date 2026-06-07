package roomescape.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class ReservationSlot {
    private final ReservationSlotInfo slot;
    private final List<Reservation> reservations;

    public ReservationSlot(ReservationSlotInfo slot, List<Reservation> reservations) {
        this.slot = slot;
        this.reservations = new ArrayList<>(reservations);
    }

    public Reservation reserve(String name, LocalDateTime now) {
        validateUniqueReservation(name);
        validateNotPastReservation(now, ErrorCode.PAST_DATE_RESERVATION);

        Reservation newReservation = new Reservation(null, name, slot.slotId(), calculateStatus(), now);
        reservations.add(newReservation);
        return newReservation;
    }


    public Reservation moveIn(Reservation reservation, String name, LocalDateTime now) {
        validateReservationOwner(reservation, name);
        validateUniqueReservation(name);
        validateNotPastReservation(now, ErrorCode.UNALLOWED_UPDATE_PAST_RESERVATION);

        reservation.update(now, slot.slotId(), calculateStatus());
        reservations.add(reservation);

        return reservation;
    }

    public Reservation moveOut(Long reservationId, String name, LocalDateTime now) {
        Reservation reservation = findReservation(reservationId);
        validateReservationOwner(reservation, name);
        validateNotPastReservation(now, ErrorCode.UNALLOWED_UPDATE_PAST_RESERVATION);

        promoteReservation(reservation);
        reservations.remove(reservation);

        return reservation;
    }

    private Status calculateStatus() {
        if (reservations.isEmpty()) {
            return Status.RESERVED;
        }
        return Status.WAITING;
    }

    private void promoteReservation(Reservation reservation) {
        if (reservation.isReserved()) {
            reservations.stream()
                    .filter(Reservation::isWaiting)
                    .min((r1, r2) -> r1.getUpdateAt().compareTo(r2.getUpdateAt()))
                    .ifPresent(Reservation::promote);
        }
    }

    public Reservation deleteReservation(Long reservationId, String name, LocalDateTime now) {
        Reservation reservation = findReservation(reservationId);
        validateReservationOwner(reservation, name);
        validateNotPastReservation(now, ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION);

        promoteReservation(reservation);
        reservation.cancel(now);
        reservations.remove(reservation);
        return reservation;
    }

    public int calculateOrder(Reservation reservation) {
        if (reservation.isReserved()) {
            return 0;
        }
        return (int) reservations.stream()
                .filter(Reservation::isWaiting)
                .filter(it -> it.isUpdatedAtBefore(reservation))
                .count() + 1;
    }

    private Reservation findReservation(Long reservationId) {
        return reservations.stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst()
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_RESERVATION));
    }

    private void validateNotPastReservation(LocalDateTime now, ErrorCode code) {
        LocalDateTime reservationTime = LocalDateTime.of(slot.date(), slot.time().getStartAt());
        if (reservationTime.isBefore(now)) {
            throw new CustomException(code);
        }
    }

    private void validateReservationOwner(Reservation reservation, String name) {
        if (!reservation.getName().equals(name)) {
            throw new CustomException(ErrorCode.COMMON_UNAUTHORIZED);
        }
    }

    private void validateUniqueReservation(String name) {
        boolean duplicated = reservations.stream()
                .anyMatch(reservation -> reservation.getName().equals(name));

        if (duplicated) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_RESERVATION);
        }
    }

    public Optional<Reservation> getReservedReservation() {
        return reservations.stream()
                .filter(r -> r.getStatus() == Status.RESERVED)
                .findFirst();
    }

    public ReservationSlotInfo getSlot() {
        return slot;
    }
}
