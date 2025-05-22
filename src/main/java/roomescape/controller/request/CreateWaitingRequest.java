package roomescape.controller.request;

import jakarta.validation.constraints.NotNull;
import roomescape.service.param.CreateWaitingParam;

import java.time.LocalDate;

public record CreateWaitingRequest(
        @NotNull(message = "날짜는 필수 값입니다.")
        LocalDate date,

        @NotNull(message = "예약 시간은 필수 값입니다.")
        Long timeId,

        @NotNull(message = "테마는 필수 값입니다.")
        Long themeId
) {
    public CreateWaitingParam toServiceParam(Long memberId) {
        return new CreateWaitingParam(memberId, date, timeId, themeId);
    }
}
