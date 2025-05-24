package roomescape.waiting.application.dto;

import roomescape.waiting.Waiting;

public record WaitingIdResponse(Long waitingId) {

    public static WaitingIdResponse from(Waiting waiting) {
        return new WaitingIdResponse(waiting.getId());
    }
}
