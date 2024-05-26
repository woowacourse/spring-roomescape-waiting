package roomescape.domain.dto;

import roomescape.domain.reservation.Reservation;

public record ReservationWithRank(Reservation reservation, long rank) {
    public boolean isReserved() {
        return reservation.isReserved();
    }

    public boolean isWaiting() {
        return reservation.isWaiting();
    }
}
