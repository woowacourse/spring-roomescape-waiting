package roomescape.reservation.domain;

public class WaitingWithRank {

    private Waiting waiting;
    private long rank;

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
