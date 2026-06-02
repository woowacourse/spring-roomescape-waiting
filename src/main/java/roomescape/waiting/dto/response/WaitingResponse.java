package roomescape.waiting.dto.response;

import roomescape.waiting.Waiting;

public record WaitingResponse(
        Long id,
        Long memberId,
        Long slotId,
        Long waitingOrder
) {

    public static WaitingResponse of(Waiting waiting, long waitingOrder) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMemberId(),
                waiting.getSlotId(),
                waitingOrder
        );
    }
}
