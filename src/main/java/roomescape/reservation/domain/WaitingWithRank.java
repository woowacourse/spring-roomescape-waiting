package roomescape.reservation.domain;

public class WaitingWithRank {

    private Reservation waiting;
    private Long rank;

    protected WaitingWithRank() {
    }

    public WaitingWithRank(Reservation waiting, int rank) {
        this.waiting = waiting;
        this.rank = (long) rank;
    }

    public WaitingWithRank(Reservation waiting, Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Reservation getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }
}
