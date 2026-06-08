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

    public int findWaitingNumber(Reservation target) {
        validateWaitingNumberTarget(target);
        int waitingNumber = 1;
        for (Reservation waiting : waitings) {
            if (waiting.isSameReservation(target)) {
                return waitingNumber;
            }
            waitingNumber++;
        }
        throw new IllegalArgumentException("예약 대기를 찾을 수 없습니다.");
    }

    public boolean isEmpty() {
        return waitings.isEmpty();
    }

    public Optional<Reservation> promoteFirstWaiting() {
        if (isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(waitings.getFirst().promote());
    }

    private void validateDuplicated(String name) {
        if (hasSameReservationOwner(name) || hasSameWaitingOwner(name)) {
            throw new DuplicateException("이미 예약 또는 대기 중인 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
        }
    }

    private ReservationStatus decideReservationStatus() {
        if (reservedReservation != null) {
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

        return reservedReservations.stream()
                .findFirst()
                .orElse(null);
    }

    private List<Reservation> findWaitings(List<Reservation> reservations) {
        return reservations.stream()
                .filter(Reservation::isWaiting)
                .sorted(Comparator.comparing(Reservation::getCreatedAt)
                        .thenComparing(Reservation::getId))
                .toList();
    }

    private boolean hasSameReservationOwner(String name) {
        return reservedReservation != null && reservedReservation.isOwner(name);
    }

    private boolean hasSameWaitingOwner(String name) {
        return waitings.stream()
                .anyMatch(waiting -> waiting.isOwner(name));
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
            throw new IllegalArgumentException("대기 순번은 같은 예약 슬롯에 대해서만 계산 가능합니다.");
        }
    }

    private void validateWaitingNumberTarget(Reservation target) {
        if (target == null) {
            throw new IllegalArgumentException("대기 순번 계산을 위해 예약 대기는 필수입니다.");
        }
        if (!target.getSlot().hasSameSlot(slot)) {
            throw new IllegalArgumentException("대기 순번은 같은 예약 슬롯에 대해서만 계산 가능합니다.");
        }
    }
}
