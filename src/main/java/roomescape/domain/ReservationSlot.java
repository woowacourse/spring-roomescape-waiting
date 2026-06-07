package roomescape.domain;

import java.time.LocalDateTime;
import java.util.List;

import roomescape.domain.vo.Slot;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class ReservationSlot {
    private final Long id;
    private final Slot slot;
    private List<Reservation> reservations;

    public ReservationSlot(Long id, Slot slot, List<Reservation> reservations) {
        this.id = id;
        this.slot = slot;
        this.reservations = reservations;
    }

    private void validateTime(LocalDateTime now) {
        if (now.isAfter(LocalDateTime.of(slot.date(), slot.startAt().getStartAt()))) {
            throw new CustomException(ErrorCode.ALREADY_PAST_RESERVATION_SLOT);
        }
    }

    private void validateUniqueReservation(String name) {
        boolean duplicated = reservations.stream()
                .anyMatch(reservation -> reservation.getName().equals(name));

        if (duplicated) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_RESERVATION);
        }
    }

    private Status calculateStatus() {
        if (reservations.isEmpty()) {
            return Status.RESERVED;
        }
        return Status.WAITING;
    }

    private void promoteReservation(Reservation reservation) {
        if (!reservation.isReserved()) {
            return;
        }

        reservations.stream()
                .filter(Reservation::isWaiting)
                .min((r1, r2) -> r1.getUpdateAt().compareTo(r2.getUpdateAt()))
                .ifPresent(Reservation::promote);
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

    public Reservation reserve(String name, LocalDateTime now) {
        validateTime(now);
        validateUniqueReservation(name);
        Reservation newReservation = new Reservation(null, name, this.id, calculateStatus(), now);
        reservations.add(newReservation);
        return newReservation;
    }

    public Reservation findReservation(Long reservationId) {
        return reservations.stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst()
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_RESERVATION));
    }

    public Reservation getReservedReservation() {
        return reservations.stream()
                .filter(r -> r.getStatus().equals(Status.RESERVED))
                .findFirst()
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_RESERVATION));
    }

    public Reservation updateReservation(Reservation reservation, LocalDateTime now) {
        reservation.update(now, this.id, calculateStatus());

        validateTime(now);

        reservations.add(reservation);
        return reservation;
    }

    public Reservation deleteReservation(Long reservationId, LocalDateTime now) {
        validateTime(now);
        Reservation reservation = findReservation(reservationId);
        promoteReservation(reservation);

        reservation.cancel(now);
        reservations.remove(reservation);
        return reservation;
    }

    public Long getId() {
        return id;
    }

    public Slot getSlot() {
        return slot;
    }
}
