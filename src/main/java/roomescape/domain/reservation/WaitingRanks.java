package roomescape.domain.reservation;

import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import roomescape.domain.member.Member;

public class WaitingRanks {

    private final Map<Waiting, Integer> waitingRanks;

    public WaitingRanks(Map<Waiting, Integer> waitingRanks) {
        this.waitingRanks = waitingRanks;
    }

    public static WaitingRanks of(List<Waiting> totalWaiting, Member member) {
        List<Waiting> memberWaiting = filterMemberWaiting(totalWaiting, member);

        Map<Waiting, Integer> waitingRanks = memberWaiting.stream()
                .collect(toMap(Function.identity(),
                        waiting -> WaitingRanks.countRank(totalWaiting, waiting))
                );

        return new WaitingRanks(waitingRanks);
    }

    private static int countRank(List<Waiting> totalWaiting, Waiting waiting) {
        return (int) totalWaiting.stream()
                .filter(w2 -> w2.isPriority(waiting))
                .count() + 1;
    }

    private static List<Waiting> filterMemberWaiting(List<Waiting> waitings, Member member) {
        return waitings.stream()
                .filter(waiting -> waiting.isMember(member))
                .toList();
    }

    public Map<Waiting, Integer> getWaitingRanks() {
        return Collections.unmodifiableMap(waitingRanks);
    }
}
