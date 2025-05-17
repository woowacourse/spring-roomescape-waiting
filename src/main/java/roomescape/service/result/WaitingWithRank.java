package roomescape.service.result;

import roomescape.domain.ReservationStatus;

public record WaitingWithRank(
        Long reservationId,
        ReservationStatus reservationStatus,
        long rank
) {
    public WaitingWithRank withPlusOneRank() {
        return new WaitingWithRank(reservationId, reservationStatus, rank + 1);
    }
} 
