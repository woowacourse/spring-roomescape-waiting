package roomescape.controller.request;

import jakarta.validation.constraints.NotNull;
import roomescape.service.param.CreateReservationParam;

import java.time.LocalDate;

public record CreateReservationRequest(
        @NotNull(message = "날짜는 필수값입니다.")
        LocalDate date,

        @NotNull(message = "예약 시간은 필수값입니다.")
        Long timeId,

        @NotNull(message = "테마는 필수값입니다.")
        Long themeId
) {
    public CreateReservationParam toServiceParam(Long memberId) {
        return new CreateReservationParam(memberId, date, timeId, themeId);
    }
}
