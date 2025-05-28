package roomescape.waiting.domain;

import roomescape.reservation.domain.ReservationStatus;

public class WaitingWithRank {
    private final Waiting waiting;
    private final Long rank;

    public WaitingWithRank(Waiting waiting, Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public ReservationStatus getStatus() {
        return ReservationStatus.PENDING;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }

}
