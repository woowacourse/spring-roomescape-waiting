package roomescape.controller.api.dto.request;

import roomescape.service.dto.input.ReservationInput;

public record ReservationRequest(String date, Long timeId, Long themeId) {

    public ReservationInput toInput(final long memberId) {
        return new ReservationInput(date, timeId, themeId, memberId);
    }
}
