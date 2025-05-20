package roomescape.waiting.dto;

import roomescape.reservation.domain.ReservationWaiting;

public record WaitingWithRank(ReservationWaiting reservationWaiting, long rank) {
}
