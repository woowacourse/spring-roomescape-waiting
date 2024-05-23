package roomescape.domain;

public class ReservationWaitingWithRank {

    private final Reservation reservation;
    private final Long rank;

    public ReservationWaitingWithRank(Reservation reservation,
                                      Long rank) {
        this.reservation = reservation;
        this.rank = rank;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Long getRank() {
        return rank;
    }
}
