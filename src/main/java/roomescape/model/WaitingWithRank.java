package roomescape.model;

public class WaitingWithRank {
    private Waiting waiting;
    private Long rank;

    public WaitingWithRank(Waiting waiting, Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public WaitingWithRank(Waiting waiting, int rank) {
        this(waiting, Long.valueOf(rank));
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }
}
