package roomescape.domain;

public class ReservationWaitWithRank {

    private ReservationWait reservationWait;
    private Long rank;

    public ReservationWaitWithRank(ReservationWait reservationWait, Long rank) {
        this.reservationWait = reservationWait;
        this.rank = rank;
    }

    public ReservationWait getReservationWait() {
        return reservationWait;
    }

    public Long getRank() {
        return rank;
    }
}
