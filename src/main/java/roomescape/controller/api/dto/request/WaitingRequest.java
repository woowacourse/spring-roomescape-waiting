package roomescape.controller.api.dto.request;

import roomescape.service.dto.input.WaitingInput;

public record WaitingRequest(String date, Long timeId, Long themeId) {

    public WaitingInput toInput(final long memberId) {
        return new WaitingInput(date, timeId, themeId, memberId);
    }
}
