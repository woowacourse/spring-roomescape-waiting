package roomescape.domain;

import java.util.Comparator;
import java.util.List;

public class WaitingLine {

    private final ReservationSlot slot;
    private final List<Reservation> waitings;

    public WaitingLine(ReservationSlot slot, List<Reservation> waitings) {
        validateSlot(slot);
        validateWaitings(waitings);
        this.slot = slot;
        validateSameSlot(waitings);
        this.waitings = waitings.stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt)
                        .thenComparing(Reservation::getId))
                .toList();
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

    public Reservation promoteFirstWaiting() {
        Reservation firstWaiting = waitings.getFirst();
        return firstWaiting.promote();
    }

    private void validateSlot(ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("예약 슬롯은 필수입니다.");
        }
    }

    private void validateWaitings(List<Reservation> waitings) {
        if (waitings == null) {
            throw new IllegalArgumentException("예약 대기 목록은 필수입니다.");
        }
    }

    private void validateSameSlot(List<Reservation> waitings) {
        boolean hasDifferentSlot = waitings.stream()
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
