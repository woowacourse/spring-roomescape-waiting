package roomescape.domain.reservation;

public class WaitingWithRank {

    public static final int RANK_OFFSET = 1;
    
    private final Waiting waiting;
    private final Long rank;

    public WaitingWithRank(Waiting waiting, Long rank) {
        this.waiting = waiting;
        this.rank = rank + RANK_OFFSET;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }
}
