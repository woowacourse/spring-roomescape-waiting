package roomescape.domain.reservation.dto;

import roomescape.domain.reservation.Reservation;

public record WaitingWithRankDto(Reservation reservation, Long rank) {
}
