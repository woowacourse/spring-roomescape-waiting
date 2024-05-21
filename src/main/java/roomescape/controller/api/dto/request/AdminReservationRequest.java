package roomescape.controller.api.dto.request;

import roomescape.service.dto.input.ReservationInput;

public record AdminReservationRequest(long memberId, long themeId, String date, long timeId) {
    public ReservationInput toInput() {
        return new ReservationInput(date, timeId, themeId, memberId);
    }
}
