package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
    @NotNull(message = "null일 수 없습니다.")
    Long memberId,

    @NotBlank(message = "null이거나 비어있을 수 없습니다.")
    String date,

    @NotNull(message = "null일 수 없습니다.")
    Long timeId,

    @NotNull(message = "null일 수 없습니다.")
    Long themeId
) { }
