package roomescape.domain.reservation;

public class ReservationResult {
    private final Rank rank;
    private final Reservation reservation;

    public ReservationResult(Rank rank, Reservation reservation) {
        this.rank = rank;
        this.reservation = reservation;
    }

    public Rank getRank() {
        return rank;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
