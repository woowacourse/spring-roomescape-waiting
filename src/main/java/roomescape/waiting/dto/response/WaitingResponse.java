package roomescape.waiting.dto.response;

import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public record WaitingResponse(Long id, WaitingStatus status) {
    public static WaitingResponse from(Waiting save) {
        return new WaitingResponse(save.getId(), save.getStatus());
    }
}
