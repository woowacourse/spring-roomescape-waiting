package roomescape.service.result;

import roomescape.domain.Reservation;

public record ReservationWithWaitingResult(
        ReservationResult reservationResult,
        WaitingWithRank waitingWithRank
) {
    public static ReservationWithWaitingResult from(Reservation reservation, int rank) {
        return new ReservationWithWaitingResult(
                ReservationResult.from(reservation),
                new WaitingWithRank(reservation.getStatus(), rank)
        );
    }
} 
