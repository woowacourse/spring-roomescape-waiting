package roomescape.reservation.domain;

public class WaitingWithRank {
    private Waiting waiting;
    private Long rank;

    public WaitingWithRank() {
    }

    public WaitingWithRank(final Waiting waiting, final Long rank) {
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
