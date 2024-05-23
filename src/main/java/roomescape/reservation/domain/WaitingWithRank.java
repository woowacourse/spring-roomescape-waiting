package roomescape.reservation.domain;

public class WaitingWithRank {

    private Waiting waiting;
    private Rank rank;

    public WaitingWithRank(Waiting waiting, Rank rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Rank getRank() {
        return rank;
    }
}
