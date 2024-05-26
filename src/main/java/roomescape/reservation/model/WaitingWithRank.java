package roomescape.reservation.model;

public class WaitingWithRank {
    private final Waiting waiting;
    private final long rank;

    public WaitingWithRank(Waiting waiting, long rank) {
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
