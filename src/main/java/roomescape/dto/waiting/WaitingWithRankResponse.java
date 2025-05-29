package roomescape.dto.waiting;

import roomescape.domain.waiting.WaitingWithRank;

public record WaitingWithRankResponse(
    WaitingResponse waitingResponse,
    Long rank
) {
    public static WaitingWithRankResponse from(WaitingWithRank waitingWithRank) {
        return new WaitingWithRankResponse(WaitingResponse.from(waitingWithRank.getWaiting()),
                waitingWithRank.getRank());
    }
}
