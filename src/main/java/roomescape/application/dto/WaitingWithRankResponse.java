package roomescape.application.dto;

import roomescape.domain.dto.WaitingWithRankDto;

public record WaitingWithRankResponse(WaitingResponse waitingResponse, Long rank) {

    public static WaitingWithRankResponse from(WaitingWithRankDto waitingWithRankDto) {
        return new WaitingWithRankResponse(
                WaitingResponse.from(waitingWithRankDto.waiting()),
                waitingWithRankDto.rank()
        );
    }
}
