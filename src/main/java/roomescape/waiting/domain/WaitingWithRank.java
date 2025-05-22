package roomescape.waiting.domain;

public class WaitingWithRank {

    private Waiting waiting;
    private long rank;

    public WaitingWithRank(final Waiting waiting, final long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public long getRank() {
        return rank;
    }
}
