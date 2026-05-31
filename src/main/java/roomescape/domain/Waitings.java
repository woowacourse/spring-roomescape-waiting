package roomescape.domain;

import java.util.List;

public class Waitings {

    private final List<Waiting> waitings;

    public Waitings(List<Waiting> waitings) {
        this.waitings = List.copyOf(waitings);
    }

    public List<WaitingWithRank> rankedBy(Member member) {
        return waitings.stream()
                .filter(waiting -> waiting.isOwnedBy(member))
                .map(waiting -> new WaitingWithRank(waiting.id(), waiting.owner(), waiting.slot(), rankOf(waiting)))
                .toList();
    }

    public int rankOf(Waiting target) {
        long ahead = waitings.stream()
                .filter(target::isSameSlot)
                .filter(waiting -> waiting.isAheadOf(target))
                .count();
        return (int) ahead + 1;
    }
}
