package roomescape.waiting.application.dto;

import roomescape.waiting.Waiting;

public record WaitingResponse(Long waitingId) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId());
    }
}
