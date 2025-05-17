package roomescape.service.result;

import roomescape.domain.ReservationStatus;

public record WaitingWithRank(
        ReservationStatus reservationStatus,
        int rank
) {
} 
