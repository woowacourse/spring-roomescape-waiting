package roomescape.reservation.waiting.domain;

import roomescape.reservation.domain.ReservationStatus;

public class WaitingWithRank {
    private final Waiting waiting;
    private final long rank;

    public WaitingWithRank(final Waiting waiting, final long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }

    public String getDescription(ReservationStatus status) {
        return (rank + 1) + "번째" + " " + status.getDescription();
    }
}
