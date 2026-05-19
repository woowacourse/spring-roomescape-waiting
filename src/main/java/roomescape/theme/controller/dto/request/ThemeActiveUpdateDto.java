package roomescape.theme.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record ThemeActiveUpdateDto(
        @NotNull(message = "isActive는 필수 입력입니다.")
        Boolean isActive
) {
}
