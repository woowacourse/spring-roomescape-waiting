package roomescape.domain.dto;

import roomescape.domain.reservation.Reservation;

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
