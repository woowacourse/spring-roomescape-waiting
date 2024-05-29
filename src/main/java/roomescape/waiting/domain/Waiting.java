package roomescape.waiting.domain;

import roomescape.reservation.domain.Reservation;

public class Waiting {

    private final Reservation reservation;

    private final int count;

    public Waiting(Reservation reservation, int count) {
        this.reservation = reservation;
        this.count = count;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public int getCount() {
        return count;
    }
}
