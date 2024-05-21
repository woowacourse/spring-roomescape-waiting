package roomescape.domain;

public class ReservationRank {

    private final Reservation reservation;
    private final long rank;

    public ReservationRank(Reservation reservation, long rank) {
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
