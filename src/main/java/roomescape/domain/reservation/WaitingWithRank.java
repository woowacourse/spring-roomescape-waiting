package roomescape.domain.reservation;

import java.util.List;

public record WaitingWithRank(Waiting waiting, int rank) {

    public WaitingWithRank(Waiting waiting, List<Waiting> sameTimeWaitings) {
        this(waiting, sameTimeWaitings.stream().sorted(WaitingWithRank::sortWaiting).toList().indexOf(waiting) + 1);
    }

    private static int sortWaiting(Waiting o1, Waiting o2) {
        return o1.getCreatedAt().compareTo(o2.getCreatedAt());
    }
}
