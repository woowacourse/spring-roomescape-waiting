package roomescape.domain.reservation;

public class WaitingWithRank {
    private Waiting waiting;
    private Long rank;

    public WaitingWithRank() {
    }

    public WaitingWithRank(Waiting waiting, Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return "WaitingWithRank{" +
                "waiting=" + waiting +
                ", rank=" + rank +
                '}';
    }
}
