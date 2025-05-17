package roomescape.controller.request;

import roomescape.service.param.CreateReservationParam;

import java.time.LocalDate;

public record CreateReservationRequest(
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public CreateReservationParam toServiceParam(Long memberId) {
        return new CreateReservationParam(memberId, date, timeId, themeId);
    }
}
