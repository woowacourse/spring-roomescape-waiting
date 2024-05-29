package roomescape.domain;

import roomescape.entity.Reservation;

public record Waiting(Reservation reservation, long rank) {
    public boolean isOver() {
        if (rank == 0) {
            reservation.confirm();
            return true;
        }
        return false;
    }
}
