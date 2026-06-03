package roomescape.waiting;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class WaitingLine {

    private final List<Waiting> waitings;

    public static WaitingLine of(List<Waiting> waitings) {
        return new WaitingLine(waitings);
    }

    private WaitingLine(List<Waiting> waitings) {
        this.waitings = waitings.stream()
                .sorted(Comparator.comparing(Waiting::getId))
                .toList();
    }

    public long orderOf(Waiting waiting) {
        for (int index = 0; index < waitings.size(); index++) {
            if (hasSameId(waitings.get(index), waiting)) {
                return index + 1L;
            }
        }
        throw new IllegalArgumentException("대기열에 존재하지 않는 대기입니다.");
    }

    private boolean hasSameId(Waiting source, Waiting target) {
        return source.getId().equals(target.getId());
    }

    public Optional<Waiting> first() {
        return waitings.stream()
                .findFirst();
    }
}
