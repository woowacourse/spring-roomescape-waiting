package roomescape.controller.request;

import roomescape.service.param.CreateWaitingParam;

import java.time.LocalDate;

public record CreateWaitingRequest(
        LocalDate date,
        Long time,
        Long theme
) {
    public CreateWaitingParam toServiceParam(Long memberId) {
        return new CreateWaitingParam(memberId, date, time, theme);
    }
}
