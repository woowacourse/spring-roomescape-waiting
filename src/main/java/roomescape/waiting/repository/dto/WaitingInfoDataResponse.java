package roomescape.waiting.repository.dto;

import roomescape.waiting.domain.Rank;
import roomescape.waiting.domain.Waiting;

public record WaitingInfoDataResponse(
        Waiting waiting,
        Rank rank
) {
    public WaitingInfoDataResponse(Waiting waiting, Long rank) {
        this(waiting, new Rank(rank));
    }
}
