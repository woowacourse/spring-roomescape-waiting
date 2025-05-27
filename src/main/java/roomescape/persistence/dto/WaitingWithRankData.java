package roomescape.persistence.dto;

import roomescape.domain.Waiting;

public record WaitingWithRankData(
        Waiting waiting,
        long rank
) {
}
