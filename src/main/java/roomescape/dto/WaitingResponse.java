package roomescape.dto;

import roomescape.domain.Waiting;

public record WaitingResponse(long id, String name, int order) {
    public static WaitingResponse from(Waiting waiting, int order) {
        return new WaitingResponse(waiting.getId(), waiting.getName(), order);
    }
}
