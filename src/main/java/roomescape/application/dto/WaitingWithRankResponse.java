package roomescape.application.dto;

import roomescape.domain.dto.WaitingWithRank;

public record WaitingWithRankResponse(WaitingResponse waitingResponse, Long rank) {

    public static WaitingWithRankResponse from(WaitingWithRank waitingWithRank) {
        return new WaitingWithRankResponse(
                WaitingResponse.from(waitingWithRank.waiting()),
                Long.valueOf(waitingWithRank.rank())
        );
    }
}
