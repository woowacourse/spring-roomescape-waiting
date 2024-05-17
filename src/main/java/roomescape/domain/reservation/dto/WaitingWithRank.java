package roomescape.domain.reservation.dto;

import roomescape.domain.reservation.Waiting;

public record WaitingWithRank(
        Waiting waiting,
        Long rank
) {
}
