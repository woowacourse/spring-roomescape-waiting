package roomescape.core.domain;

public class WaitingWithRank {
    private Long rank;
    private Waiting waiting;

    public WaitingWithRank(final Long rank, final Waiting waiting) {
        this.rank = rank;
        this.waiting = waiting;
    }

    public Long getRank() {
        return rank;
    }

    public Waiting getWaiting() {
        return waiting;
    }
}
