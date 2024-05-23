package roomescape.reservation.domain;

public class WaitingWithRank {
    private final Reservation waiting;
    private final Long rank;

    public WaitingWithRank(final Reservation waiting, final Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public WaitingWithRank(final Reservation waiting, final Integer rank) {
        this(waiting, (long) rank);
    }

    public Reservation getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }
}
