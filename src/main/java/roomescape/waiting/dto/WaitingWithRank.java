package roomescape.waiting.dto;

import roomescape.waiting.domain.ReservationWaiting;

public record WaitingWithRank(ReservationWaiting reservationWaiting, long rank) {
}
