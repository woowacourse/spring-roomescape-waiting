package roomescape.domain.waiting;

import java.util.Objects;

public class WaitingWithRank {
    private final Waiting waiting;
    private final Long rank;

    public WaitingWithRank(Waiting waiting, long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WaitingWithRank that = (WaitingWithRank) o;
        return Objects.equals(waiting, that.waiting) && Objects.equals(rank, that.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waiting, rank);
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }
}
