package roomescape.controller.request;

import roomescape.service.param.CreateBookingParam;

import java.time.LocalDate;

public record CreatBookingRequest(
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public CreateBookingParam toServiceParam(Long memberId) {
        return new CreateBookingParam(memberId, date, timeId, themeId);
    }
}
