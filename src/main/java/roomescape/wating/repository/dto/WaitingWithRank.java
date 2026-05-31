package roomescape.wating.repository.dto;

import roomescape.wating.domain.Waiting;

public record WaitingWithRank(
        Waiting waiting,
        int rank
) {
}
