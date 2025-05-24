package roomescape.reservation.waiting.domain.dto;

import roomescape.reservation.waiting.domain.WaitingReservation;

public record WaitingReservationWithRank(WaitingReservation waitingReservation, long rank) {
}
