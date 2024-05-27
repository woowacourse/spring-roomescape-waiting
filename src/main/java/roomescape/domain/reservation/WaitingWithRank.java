package roomescape.domain.reservation;

public class WaitingWithRank {

    private static final String ORDER_STATUS_FORMAT = "%d번째 예약대기";

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

    public String formatOrderStatus() {
        return ORDER_STATUS_FORMAT.formatted(rank);
    }

    @Override
    public String toString() {
        return "WaitingWithRank{" +
                "waiting=" + waiting +
                ", rank=" + rank +
                '}';
    }
}
