package roomescape.reservation.domain;

import roomescape.reservation.exception.ReservationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_BOOKED;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_NOT_FOUND;

public record Reservations(
        List<Reservation> values
) {

    public Reservations {
        values = new ArrayList<>(values);
    }

    // TODO RESERVED, WAITING 판단 로직 수정 이상함.
    public Reservation reserve(String requesterName, Long slotId, LocalDateTime reservedAt) {
        validateNotAlreadyBookedBy(requesterName);
        if (hasReservedByOthers(requesterName)) {
            return register(Reservation.wait(requesterName, slotId, reservedAt));
        }
        return register(Reservation.reserve(requesterName, slotId, reservedAt));
    }

    // TODO reserve()에 재활용하기.
    public ReservationStatus decideStatus(String requesterName) {
        validateNotAlreadyBookedBy(requesterName);

        if (hasReservedByOthers()) {
            return ReservationStatus.WAITING;
        }

        return ReservationStatus.RESERVED;
    }

    public Reservations cancel(String requesterName) {
        Reservation cancelTarget = findByName(requesterName);

        if (cancelTarget.isReserved()) {
            cancelTarget.cancel(requesterName);
            return promoteWaiting().map(promoting -> new Reservations(List.of(cancelTarget, promoting)))
                    .orElseGet(() -> new Reservations(List.of(cancelTarget)));
        }

        cancelTarget.cancel(requesterName);
        return new Reservations(List.of(cancelTarget));
    }

    public Reservations cancelByManager(String requesterName) {
        Reservation cancelTarget = findByName(requesterName);

        if (cancelTarget.isReserved()) {
            cancelTarget.cancelByManager();
            return promoteWaiting().map(promoting -> new Reservations(List.of(cancelTarget, promoting)))
                    .orElseGet(() -> new Reservations(List.of(cancelTarget)));
        }

        cancelTarget.cancelByManager();
        return new Reservations(List.of(cancelTarget));
    }

    public Reservations reschedule(Long newSlotId, String requesterName, ReservationStatus status) {
        Reservation target = popByName(requesterName);

        if (target.isReserved()) {
            target.reschedule(newSlotId, requesterName, status);
            return promoteWaiting().map(promoting -> new Reservations(List.of(target, promoting)))
                    .orElseGet(() -> new Reservations(List.of(target)));
        }

        target.reschedule(newSlotId, requesterName, status);
        return new Reservations(List.of(target));
    }

    public Reservations rescheduleByManager(Long newSlotId, String requesterName, ReservationStatus status) {
        Reservation target = popByName(requesterName);

        if (target.isReserved()) {
            target.rescheduleByManager(newSlotId, status);
            return promoteWaiting().map(promoting -> new Reservations(List.of(target, promoting)))
                    .orElseGet(() -> new Reservations(List.of(target)));
        }

        target.rescheduleByManager(newSlotId, status);
        return new Reservations(List.of(target));
    }

    public void validateNotAlreadyBookedBy(String requestName) {
        boolean alreadyBookedByMyself = values.stream()
                .anyMatch(reservation -> reservation.isOwner(requestName));

        if (alreadyBookedByMyself) {
            throw new ReservationException(RESERVATION_ALREADY_BOOKED);
        }
    }

    public Optional<Reservation> promoteWaiting() {
        return values.stream()
                .filter(Reservation::isWaiting)
                .min(Comparator.comparing(Reservation::getReservedAt).thenComparing(Reservation::getId))
                .map(Reservation::promote);
    }

    public boolean hasReservedByOthers(String name) {
        return values.stream()
                .anyMatch(reservation -> !reservation.isOwner(name) && reservation.isReserved());
    }

    public boolean hasReservedByOthers() {
        return values.stream()
                .anyMatch(Reservation::isReserved);
    }

    private Reservation register(Reservation reservation) {
        values.add(reservation);
        return reservation;
    }

    public Reservation findByName(String requesterName) {
        return values.stream()
                .filter(r -> r.isOwner(requesterName))
                .findFirst()
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));
    }

    private Reservation popByName(String requesterName) {
        Reservation target = findByName(requesterName);
        values.remove(target);
        return target;
    }

    public Reservation findById(Long reservationId) {
        return values.stream()
                .filter(r -> r.getId() != null && r.getId().equals(reservationId))
                .findFirst()
                .orElseThrow(() -> new ReservationException(RESERVATION_NOT_FOUND));
    }

}
