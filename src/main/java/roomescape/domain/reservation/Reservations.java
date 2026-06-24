package roomescape.domain.reservation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Reservations {

    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        validateReservationCount(reservations);
        validateWaitings(reservations);
        this.reservations = new ArrayList<>(reservations);
    }

    public Reservation add(String name, ReservationSlot slot, LocalDateTime now) {
        Reservation reservation = new Reservation(name, slot, now, decideReservationStatus());
        reservations.add(reservation);
        return reservation;
    }

    private boolean hasReservedReservation() {
        return findReservedReservation().isPresent();
    }

    public boolean hasSameReservationOwner(String name) {
        return findReservedReservation()
                .map(reservedReservation -> reservedReservation.isOwner(name))
                .orElse(false);
    }

    public boolean hasSameWaitingOwner(String name) {
        return findWaitings().stream()
                .anyMatch(waiting -> waiting.isOwner(name));
    }

    public boolean containsReservation(Reservation target) {
        return isReservedReservation(target) || isWaitingReservation(target);
    }

    public Optional<Integer> findWaitingIndex(Reservation target) {
        List<Reservation> waitings = findSortedWaitings();
        return waitings.stream()
                .filter(waiting -> waiting.isSameReservation(target))
                .findFirst()
                .map(waitings::indexOf);
    }

    public Optional<Reservation> findPromotedFirstWaiting() {
        return findSortedWaitings().stream()
                .findFirst()
                .map(Reservation::promote);
    }

    public ReservationStatus decideReservationStatus() {
        if (hasReservedReservation()) {
            return ReservationStatus.WAITING;
        }
        return ReservationStatus.RESERVED;
    }

    private Optional<Reservation> findReservedReservation() {
        return reservations.stream()
                .filter(Reservation::isReserved)
                .findFirst();
    }

    private List<Reservation> findWaitings() {
        return reservations.stream()
                .filter(Reservation::isWaiting)
                .toList();
    }

    private List<Reservation> findSortedWaitings() {
        return findWaitings().stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt)
                        .thenComparing(Reservation::getId))
                .toList();
    }

    private boolean isWaitingReservation(Reservation target) {
        return findWaitings().stream()
                .anyMatch(waiting -> waiting.isSameReservation(target));
    }

    private boolean isReservedReservation(Reservation target) {
        return findReservedReservation()
                .map(reservedReservation -> reservedReservation.isSameReservation(target))
                .orElse(false);
    }

    private void validateReservationCount(List<Reservation> reservations) {
        int reservationCount = (int) reservations.stream()
                .filter(Reservation::isReserved)
                .count();

        if (reservationCount > 1) {
            throw new IllegalArgumentException("하나의 예약 슬롯에는 확정 예약이 하나만 존재할 수 있습니다.");
        }
    }

    private void validateWaitings(List<Reservation> reservations) {
        boolean hasReserved = reservations.stream()
                .anyMatch(Reservation::isReserved);

        boolean hasWaiting = reservations.stream()
                .anyMatch(Reservation::isWaiting);

        if (!hasReserved && hasWaiting) {
            throw new IllegalArgumentException("확정 예약 없이 예약 대기만 존재할 수 없습니다.");
        }
    }
}
