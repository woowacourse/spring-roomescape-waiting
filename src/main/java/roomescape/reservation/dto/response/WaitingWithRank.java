package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Waiting;

public class WaitingWithRank {
    private final Waiting waiting;
    private final Long rank;

    public WaitingWithRank(Waiting waiting, Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }
}
