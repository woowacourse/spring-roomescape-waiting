package roomescape.dto.query;

import roomescape.entity.WaitingReservation;

public class WaitingWithRank {
    private WaitingReservation waitingReservation;
    private Long rank;

    public WaitingWithRank(WaitingReservation waitingReservation, Long rank) {
        this.waitingReservation = waitingReservation;
        this.rank = rank;
    }

    public WaitingReservation getWaiting() {
        return waitingReservation;
    }

    public Long getRank() {
        return  rank;
    }
}
