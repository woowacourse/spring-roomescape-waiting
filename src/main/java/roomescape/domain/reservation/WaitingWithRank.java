package roomescape.domain.reservation;

public class WaitingWithRank {
    private final ReservationWaiting waiting;
    private final Long rank;

    public WaitingWithRank(ReservationWaiting waiting, Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public ReservationWaiting getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }
}
