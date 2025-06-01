package roomescape.waiting.dto;

import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public class WaitingWithRank {

    private final Waiting waiting;
    private final Long rank;

    public WaitingWithRank(Waiting waiting, Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public String getLabel() {
        WaitingStatus status = waiting.getStatus();
        if (status == WaitingStatus.PENDING) {
            return rank + "번째 " + status.getLabel();
        }
        return status.getLabel();
    }
}
