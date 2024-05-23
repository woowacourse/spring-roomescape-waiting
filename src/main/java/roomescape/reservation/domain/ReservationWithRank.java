package roomescape.reservation.domain;

public class ReservationWithRank {

    private Reservation reservation;
    private Long rank;

    protected ReservationWithRank() {
    }

    public ReservationWithRank(Reservation reservation, long rank) {
        this.reservation = reservation;
        this.rank = rank;
    }

    public Reservation getWaiting() {
        return reservation;
    }

    public Long getRank() {
        return rank;
    }
}
