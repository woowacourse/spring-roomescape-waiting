package roomescape.domain;

import java.util.Comparator;
import java.util.List;

public class WaitingLine {

    private final List<Waiting> waitings;

    public WaitingLine(List<Waiting> waitings) {
        validateEmpty(waitings);
        validateSameSlot(waitings);
        this.waitings = waitings.stream()
                .sorted(Comparator.comparing(Waiting::getCreatedAt)
                        .thenComparing(Waiting::getId))
                .toList();
    }

    private void validateEmpty(List<Waiting> waitings) {
        if (waitings == null || waitings.isEmpty()) {
            throw new IllegalArgumentException("예약 대기가 없습니다.");
        }
    }

    private void validateSameSlot(List<Waiting> waitings) {
        Waiting waiting = waitings.getFirst();

        boolean hasDifferentSlot = waitings.stream()
                .anyMatch(target -> !target.hasSameSlot(waiting));

        if (hasDifferentSlot) {
            throw new IllegalArgumentException("대기 순번은 같은 예약 슬롯에 대해서만 계산 가능합니다.");
        }
    }

    public int findWaitingNumber(Waiting target) {
        validateWaitingNumberTarget(target);
        int waitingNumber = 1;
        for (Waiting waiting : waitings) {
            if (waiting.isSameWaiting(target)) {
                return waitingNumber;
            }
            waitingNumber++;
        }
        throw new IllegalArgumentException("예약 대기를 찾을 수 없습니다.");
    }

    private void validateWaitingNumberTarget(Waiting target) {
        if (target == null) {
            throw new IllegalArgumentException("대기 순번 계산을 위해 예약 대기는 필수입니다.");
        }
        if (!target.hasSameSlot(waitings.getFirst())) {
            throw new IllegalArgumentException("대기 순번은 같은 예약 슬롯에 대해서만 계산 가능합니다.");
        }
    }
}
