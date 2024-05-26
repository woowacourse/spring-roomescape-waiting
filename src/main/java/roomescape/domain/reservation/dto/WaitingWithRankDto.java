package roomescape.domain.reservation.dto;

import roomescape.domain.reservation.Waiting;

public record WaitingWithRankDto(Waiting waiting, Long rank) {
}
