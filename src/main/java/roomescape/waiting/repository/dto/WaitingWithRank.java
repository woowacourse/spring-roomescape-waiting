package roomescape.waiting.repository.dto;

import roomescape.waiting.domain.Waiting;

public record WaitingWithRank(
    Waiting waiting,
    int rank
) {
}
