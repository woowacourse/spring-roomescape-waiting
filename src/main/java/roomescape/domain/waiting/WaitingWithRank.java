package roomescape.domain.waiting;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class WaitingWithRank {
    private final Waiting waiting;
    private final long rank;

    public WaitingWithRank(Waiting waiting, long previousWaitingCount) {
        this.waiting = waiting;
        this.rank = previousWaitingCount + 1;
    }
}
