package roomescape.waiting.domain;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.IntStream;

public class Waitings {
    private final Queue<Waiting> waitings;

    public Waitings(List<Waiting> waitings) {
        this.waitings = new PriorityQueue<>(Comparator.comparing(Waiting::getCreatedAt));
        this.waitings.addAll(waitings);
    }

    public Waiting pollHighestPriority() {
        return waitings.poll();
    }

    public List<WaitingWithRank> getWaitingsWithRank() {
        List<Waiting> waitings = this.waitings.stream()
                .sorted(Comparator.comparing(Waiting::getCreatedAt))
                .toList();

        return IntStream.range(0, waitings.size())
                .mapToObj(index -> new WaitingWithRank(waitings.get(index), index + 1L))
                .toList();

    }
}
