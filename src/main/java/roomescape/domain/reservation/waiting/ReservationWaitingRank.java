package roomescape.domain.reservation.waiting;

public class ReservationWaitingRank {
    private final int rank;

    public ReservationWaitingRank(int rank) {
        this.rank = rank;
    }

    public String getStatusMessage() {
        return String.format("%d번째 예약대기", rank);
    }
}
