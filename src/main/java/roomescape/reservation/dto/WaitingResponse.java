package roomescape.reservation.dto;

import roomescape.reservation.domain.Waiting;

public record WaitingResponse(Long id) {

    public WaitingResponse(Waiting waiting) {
        this(waiting.getId());
    }
}
