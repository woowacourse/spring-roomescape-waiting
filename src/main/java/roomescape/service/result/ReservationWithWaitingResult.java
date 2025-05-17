package roomescape.service.result;

import roomescape.domain.Reservation;

public record ReservationWithWaitingResult(
        ReservationResult reservationResult,
        WaitingWithRank waitingWithRank
) {
    public static ReservationWithWaitingResult from(Reservation reservation, long rank) {
        return new ReservationWithWaitingResult(
                ReservationResult.from(reservation),
                new WaitingWithRank(reservation.getId(), reservation.getStatus(), rank)
        );
    }
}
