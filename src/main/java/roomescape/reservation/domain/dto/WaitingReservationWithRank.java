package roomescape.reservation.domain.dto;

import roomescape.reservation.domain.WaitingReservation;

public record WaitingReservationWithRank(WaitingReservation waitingReservation, long rank) {
}
