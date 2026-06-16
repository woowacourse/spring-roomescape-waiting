package roomescape.reservation.domain;

import roomescape.reservation.exception.ReservationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_FOUND;

public record Reservations(
        List<Reservation> values
) {

    public Reservations {
        values = new ArrayList<>(values);
    }

    public Reservation reserve(String requesterName, Long slotId, LocalDateTime reservedAt) {
        ReservationStatus status = decideStatus(requesterName);
        return register(requesterName, slotId, reservedAt, status);
    }

    public ReservationStatus decideStatus(String requesterName) {
        validateNotAlreadyBookedBy(requesterName);
        if (hasReserved()) {
            return ReservationStatus.WAITING;
        }

        return ReservationStatus.PENDING_PAYMENT;
    }

    public Reservations cancel(Long reservationId, String requesterName) {
        return processUpdateAction(reservationId, reservation -> reservation.cancel(requesterName));
    }

    public Reservations cancelByManager(Long reservationId) {
        return processUpdateAction(reservationId, reservation -> reservation.cancelByManager());
    }

    public Reservations reschedule(Long newSlotId, Long reservationId, String requesterName, ReservationStatus status) {
        return processUpdateAction(reservationId, reservation -> reservation.reschedule(newSlotId, requesterName, status));
    }

    public Reservations rescheduleByManager(Long newSlotId, Long reservationId, ReservationStatus status) {
        return processUpdateAction(reservationId, reservation -> reservation.rescheduleByManager(newSlotId, status));
    }

    private Reservations processUpdateAction(Long reservationId, Consumer<Reservation> action) {
        Reservation target = popById(reservationId);
        if (target.isReserved() || target.isPendingPayment()) {
            action.accept(target);
            return withPromotedIfPresent(target);
        }

        action.accept(target);
        return new Reservations(List.of(target));
    }

    private Reservations withPromotedIfPresent(Reservation target) {
        return promoteWaiting().map(promoting -> new Reservations(List.of(target, promoting)))
                .orElseGet(() -> new Reservations(List.of(target)));
    }

    public Optional<Reservation> promoteWaiting() {
        Optional<Reservation> waiting = values.stream()
                .filter(Reservation::isWaiting)
                .min(Comparator.comparing(Reservation::getReservedAt).thenComparing(Reservation::getId));

        waiting.ifPresent(Reservation::promote);
        return waiting;
    }

    public void validateNotAlreadyBookedBy(String requestName) {
        boolean alreadyBookedByMyself = values.stream()
                .anyMatch(reservation -> reservation.isOwner(requestName));

        if (alreadyBookedByMyself) {
            throw new ReservationException(RESERVATION_ALREADY_BOOKED);
        }
    }

    public boolean hasReserved() {
        return values.stream()
                .anyMatch(r -> r.isReserved() || r.isPendingPayment());
    }

    public Reservation findById(Long reservationId) {
        return values.stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst()
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));
    }

    private Reservation popById(Long reservationId) {
        Reservation target = findById(reservationId);
        values.remove(target);
        return target;
    }

    private Reservation register(String requesterName, Long slotId, LocalDateTime reservedAt, ReservationStatus status) {
        Reservation reserved = Reservation.reserve(requesterName, slotId, status, reservedAt);
        values.add(reserved);
        return reserved;
    }

}
