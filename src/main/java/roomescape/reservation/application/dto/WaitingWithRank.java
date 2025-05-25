package roomescape.reservation.application.dto;

import roomescape.reservation.domain.Waiting;

public record WaitingWithRank(
        Waiting waiting,
        Long rank
) {
}
