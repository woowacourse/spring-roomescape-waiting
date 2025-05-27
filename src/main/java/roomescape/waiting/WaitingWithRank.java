package roomescape.waiting;

public class WaitingWithRank {

    private final Waiting waiting;
    private final long rank;

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
