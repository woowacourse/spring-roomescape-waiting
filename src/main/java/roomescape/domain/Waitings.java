package roomescape.domain;

import java.util.Comparator;
import java.util.List;
import roomescape.exception.client.BusinessRuleViolationException;

public class Waitings {
    private final List<Waiting> waitings;

    public Waitings(List<Waiting> waitings) {
        this.waitings = waitings.stream()
                .sorted(Comparator.comparingInt(Waiting::getOrderIndex))
                .toList();
    }

    private boolean hasWaitingBy(String name) {
        return waitings.stream()
                .anyMatch(w -> w.isOwnedBy(name));
    }

    public void validateNoDuplicateBy(String name) {
        if (hasWaitingBy(name)) {
            throw new BusinessRuleViolationException("이미 해당 시간에 대기 신청한 내역이 있습니다.");
        }
    }

    public int nextOrderIndex() {
        return waitings.size() + 1;
    }

    public List<Waiting> reorderAfterRemoval(int removedOrder) {
        return waitings.stream()
                .filter(w -> w.getOrderIndex() > removedOrder)
                .map(w -> w.withOrderIndex(w.getOrderIndex() - 1))
                .toList();
    }
}
