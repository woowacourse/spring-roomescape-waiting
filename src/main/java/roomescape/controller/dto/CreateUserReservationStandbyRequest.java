package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateUserReservationStandbyRequest(
    @NotNull(message = "null일 수 없습니다.")
    String date,

    @NotNull(message = "null일 수 없습니다.")
    @Positive(message = "양수만 입력할 수 있습니다.")
    Long themeId,

    @NotNull(message = "null일 수 없습니다.")
    @Positive(message = "양수만 입력할 수 있습니다.")
    Long timeId
) { }
