package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;

public record WaitingRank(Reservation reservation, long waitingNumber) {
}
