package roomescape.waiting.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WaitingLine {

    private final List<Waiting> waitings;

    private WaitingLine(List<Waiting> waitings) {
        validateSameSlot(waitings);
        this.waitings = waitings.stream()
                .sorted(Comparator.comparing(Waiting::getId))
                .toList();
    }

    public static WaitingLine of(List<Waiting> waitings) {
        return new WaitingLine(waitings);
    }

    private void validateSameSlot(List<Waiting> waitings) {
        long slotCount = waitings.stream()
                .map(Waiting::getSlotId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        if (slotCount > 1) {
            throw new IllegalArgumentException("같은 슬롯의 대기만 대기열이 될 수 있습니다.");
        }
    }

    public long orderOf(Waiting waiting) {
        return orderOf(waiting.getId());
    }

    public long orderOf(Long waitingId) {
        for (int index = 0; index < waitings.size(); index++) {
            if (hasSameId(waitings.get(index), waitingId)) {
                return index + 1L;
            }
        }
        throw new IllegalArgumentException("대기열에 존재하지 않는 대기입니다.");
    }

    private boolean hasSameId(Waiting source, Long targetId) {
        return source.getId().equals(targetId);
    }

    public Optional<Waiting> first() {
        return waitings.stream()
                .findFirst();
    }
}
