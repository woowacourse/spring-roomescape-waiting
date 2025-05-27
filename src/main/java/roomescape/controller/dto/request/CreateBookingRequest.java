package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.service.dto.param.CreateBookingParam;

import java.time.LocalDate;

public record CreateBookingRequest(

        @NotNull
        LocalDate date,

        @NotNull
        Long timeId,

        @NotNull
        Long themeId
) {
    public CreateBookingParam toServiceParam(Long memberId) {
        return new CreateBookingParam(memberId, date, timeId, themeId);
    }
}
