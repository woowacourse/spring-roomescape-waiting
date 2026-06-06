package roomescape.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import roomescape.exception.DuplicateException;

public class ReservationLine {

    private final ReservationSlot slot;
    private final List<Reservation> reservations;
    private final List<Reservation> waitings;

    public ReservationLine(ReservationSlot slot, List<Reservation> reservations) {
        validateSlot(slot);
        validateReservations(reservations);
        this.slot = slot;
        validateSameSlot(reservations);
        this.reservations = List.copyOf(reservations);
        this.waitings = reservations.stream()
                .filter(Reservation::isWaiting)
                .sorted(Comparator.comparing(Reservation::getCreatedAt)
                        .thenComparing(Reservation::getId))
                .toList();
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
        boolean isDuplicated = reservations.stream()
                .anyMatch(reservation -> reservation.isOwner(name));
        if (isDuplicated) {
            throw new DuplicateException("이미 예약 또는 대기 중인 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
        }
    }

    private ReservationStatus decideReservationStatus() {
        boolean hasReserved = reservations.stream()
                .anyMatch(Reservation::isReserved);

        if (hasReserved) {
            return ReservationStatus.WAITING;
        }
        return ReservationStatus.RESERVED;
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
