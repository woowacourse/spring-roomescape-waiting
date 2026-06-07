package roomescape.theme.controller.dto;

import jakarta.validation.constraints.NotNull;
import roomescape.global.dto.ActivationStatus;

public record ThemeUpdateRequest(
        @NotNull(message = "테마 상태는 비어 있을 수 없습니다.")
        ActivationStatus status
) {
}
