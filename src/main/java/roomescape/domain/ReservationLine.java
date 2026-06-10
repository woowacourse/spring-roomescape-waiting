package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import roomescape.exception.DuplicateException;

public class ReservationLine {

    private final ReservationSlot slot;
    private final Reservation reservedReservation;
    private final List<Reservation> waitings;

    public ReservationLine(ReservationSlot slot, List<Reservation> reservations) {
        validateSlot(slot);
        validateReservations(reservations);
        this.slot = slot;
        validateSameSlot(reservations);
        this.reservedReservation = findReservedReservation(reservations);
        this.waitings = findWaitings(reservations);
    }

    public Reservation add(String name, LocalDateTime now) {
        validateDuplicated(name);
        return new Reservation(name, slot, now, decideReservationStatus());
    }

    public Optional<Integer> findWaitingIndex(Reservation target) {
        validateWaitingIndexTarget(target);
        return waitings.stream()
                .filter(waiting -> waiting.isSameReservation(target))
                .findFirst()
                .map(waitings::indexOf);
    }

    public Optional<Reservation> findPromotedReservationAfterCancel(Reservation target, LocalDateTime now) {
        validateReservationInLine(target);
        target.validateCancelable(now);

        return findPromotedReservation(target);
    }

    public Optional<Reservation> findReservationToPromote(Reservation target) {
        validateReservationInLine(target);

        return findPromotedReservation(target);
    }

    private Optional<Reservation> findPromotedReservation(Reservation target) {
        if (target.isWaiting()) {
            return Optional.empty();
        }
        return findPromotedFirstWaiting();
    }

    private void validateDuplicated(String name) {
        if (hasSameReservationOwner(name) || hasSameWaitingOwner(name)) {
            throw new DuplicateException("이미 예약 또는 대기 중인 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
        }
    }

    private ReservationStatus decideReservationStatus() {
        if (hasReservedReservation()) {
            return ReservationStatus.WAITING;
        }
        return ReservationStatus.RESERVED;
    }

    private Reservation findReservedReservation(List<Reservation> reservations) {
        List<Reservation> reservedReservations = reservations.stream()
                .filter(Reservation::isReserved)
                .toList();

        if (reservedReservations.size() > 1) {
            throw new IllegalArgumentException("하나의 예약 슬롯에는 확정 예약이 하나만 존재할 수 있습니다.");
        }

        if (reservedReservations.isEmpty() && hasWaiting(reservations)) {
            throw new IllegalArgumentException("확정 예약 없이 예약 대기만 존재할 수 없습니다.");
        }

        if (reservedReservations.isEmpty()) {
            return null;
        }
        return reservedReservations.getFirst();
    }

    private boolean hasWaiting(List<Reservation> reservations) {
        return reservations.stream()
                .anyMatch(Reservation::isWaiting);
    }

    private List<Reservation> findWaitings(List<Reservation> reservations) {
        return reservations.stream()
                .filter(Reservation::isWaiting)
                .sorted(Comparator.comparing(Reservation::getCreatedAt)
                        .thenComparing(Reservation::getId))
                .toList();
    }

    private boolean hasSameReservationOwner(String name) {
        return hasReservedReservation() && reservedReservation.isOwner(name);
    }

    private boolean hasSameWaitingOwner(String name) {
        return waitings.stream()
                .anyMatch(waiting -> waiting.isOwner(name));
    }

    private boolean containsReservation(Reservation target) {
        return isReservedReservation(target) || isWaitingReservation(target);
    }

    private boolean isReservedReservation(Reservation target) {
        return hasReservedReservation() && reservedReservation.isSameReservation(target);
    }

    private boolean hasReservedReservation() {
        return reservedReservation != null;
    }

    private boolean isWaitingReservation(Reservation target) {
        return waitings.stream()
                .anyMatch(waiting -> waiting.isSameReservation(target));
    }

    private Optional<Reservation> findPromotedFirstWaiting() {
        if (waitings.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(waitings.getFirst().promote());
    }

    private void validateSlot(ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("예약 슬롯은 필수입니다.");
        }
    }

    private void validateReservations(List<Reservation> reservations) {
        if (reservations == null) {
            throw new IllegalArgumentException("예약 목록은 필수입니다.");
        }
    }

    private void validateSameSlot(List<Reservation> reservations) {
        boolean hasDifferentSlot = reservations.stream()
                .anyMatch(target -> !target.getSlot().hasSameSlot(slot));

        if (hasDifferentSlot) {
            throw new IllegalArgumentException("대기 위치는 같은 예약 슬롯에 대해서만 계산 가능합니다.");
        }
    }

    private void validateWaitingIndexTarget(Reservation target) {
        validateSameSlotTarget(target);
    }

    private void validateReservationInLine(Reservation target) {
        validateSameSlotTarget(target);
        if (!containsReservation(target)) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
    }

    private void validateSameSlotTarget(Reservation target) {
        if (target == null) {
            throw new IllegalArgumentException("예약은 필수입니다.");
        }
        if (!target.getSlot().hasSameSlot(slot)) {
            throw new IllegalArgumentException("같은 예약 슬롯의 예약만 처리할 수 있습니다.");
        }
    }
}
