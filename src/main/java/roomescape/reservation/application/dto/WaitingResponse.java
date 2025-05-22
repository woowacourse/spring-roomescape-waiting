package roomescape.reservation.application.dto;

import roomescape.reservation.domain.Waiting;

public record WaitingResponse(Long waitingId) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId());
    }
}
