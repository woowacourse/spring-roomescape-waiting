package roomescape.domain;

public class ReservationWaitingWithRank {

    private final ReservationWaiting waiting;
    private final Long rank;

    public ReservationWaitingWithRank(ReservationWaiting waiting, Long rank) {
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
