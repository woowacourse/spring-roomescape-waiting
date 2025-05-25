package roomescape.waiting.domain;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import roomescape.member.domain.Member;

public class Waitings {
    private final Queue<Waiting> waitings;

    public Waitings(List<Waiting> waitings) {
        this.waitings = new PriorityQueue<>(Comparator.comparing(Waiting::getCreatedAt));
        this.waitings.addAll(waitings);
    }

    public Waiting pollHighestPriority() {
        return waitings.poll();
    }

    public boolean containsMember(Member member) {
        return waitings.stream()
                .anyMatch(waiting -> waiting.getMember().equals(member));
    }

    public long getRankOf(Waiting waiting) {
        List<Waiting> waitings = this.waitings.stream()
                .sorted(Comparator.comparing(Waiting::getCreatedAt))
                .toList();

        return waitings.indexOf(waiting) + 1L;
    }
}
