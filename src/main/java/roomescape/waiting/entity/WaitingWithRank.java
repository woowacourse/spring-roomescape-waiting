package roomescape.waiting.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaitingWithRank {

    private Waiting waiting;
    private Long rank;

    public WaitingWithRank(Waiting waiting, Integer rank) {
        this.waiting = waiting;
        this.rank = Long.valueOf(rank);
    }
}
