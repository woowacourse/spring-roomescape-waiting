package roomescape.reservation.domain.waiting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@NoArgsConstructor
@Accessors(fluent = true)
public class WaitingWithRank {

    private Waiting waiting;
    private Long rank;

    public WaitingWithRank(final Waiting waiting, final Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }
}
