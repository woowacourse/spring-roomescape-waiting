package roomescape.domain;

import java.util.Objects;

public class ReservationWithRank {

    private final Reservation reservation;
    private final Long rank;

    public ReservationWithRank(Reservation reservation, Long rank) {
        this.reservation = reservation;
        this.rank = rank;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Long getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWithRank that = (ReservationWithRank) o;
        return Objects.equals(reservation, that.reservation) && Objects.equals(rank, that.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservation, rank);
    }
}
