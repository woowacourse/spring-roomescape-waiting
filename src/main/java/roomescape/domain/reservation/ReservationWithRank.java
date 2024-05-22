package roomescape.domain.reservation;

public class ReservationWithRank {
    private Reservation reservation;
    private long rank;

    public ReservationWithRank(Reservation reservation, long rank) {
        this.reservation = reservation;
        this.rank = rank;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public long getRank() {
        return rank;
    }
}
