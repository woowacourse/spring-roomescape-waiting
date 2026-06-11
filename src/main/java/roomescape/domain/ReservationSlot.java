package roomescape.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import roomescape.domain.vo.ReservationDeletion;
import roomescape.domain.vo.ReservationSlotInfo;
import roomescape.domain.vo.ReservationUpdate;
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

    public ReservationUpdate moveOut(long reservationId, String name, LocalDateTime now) {
        Reservation reservation = findReservation(reservationId);
        validateReservationOwner(reservation, name);
        validateNotPastReservation(now, ErrorCode.UNALLOWED_UPDATE_PAST_RESERVATION);

        Optional<Reservation> promotedReservation = promoteReservation(reservation);
        reservations.remove(reservation);
        return new ReservationUpdate(reservation,promotedReservation);
    }

    private Status calculateStatus() {
        if (reservations.isEmpty()) {
            return Status.RESERVED;
        }
        return Status.WAITING;
    }

    private Optional<Reservation> promoteReservation(Reservation reservation) {
        if (!reservation.isReserved()) {
            return Optional.empty();
        }

        if (reservations.size() < 2) {
            return Optional.empty();
        }
        // ReservationSlot의 reservations는 update_at을 기준으로 정렬되어있다.
        Reservation nextReservation = reservations.get(1);
        nextReservation.promote();
        return Optional.of(nextReservation);
    }

    public ReservationDeletion deleteReservation(long reservationId, String name, LocalDateTime now) {
        Reservation reservation = findReservation(reservationId);
        validateReservationOwner(reservation, name);
        validateNotPastReservation(now, ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION);

        Optional<Reservation> promotedReservation = promoteReservation(reservation);
        reservation.cancel(now);
        reservations.remove(reservation);
        return new ReservationDeletion(reservation, promotedReservation);
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

    private Reservation findReservation(long reservationId) {
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

    public ReservationSlotInfo getSlot() {
        return slot;
    }
}
