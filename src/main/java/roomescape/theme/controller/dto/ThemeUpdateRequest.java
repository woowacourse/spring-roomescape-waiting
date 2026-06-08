package roomescape.theme.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ThemeUpdateRequest(
        @NotNull(message = "테마 상태는 비어 있을 수 없습니다.")
        @Pattern(regexp = "ACTIVE|INACTIVE", message = "테마 상태는 ACTIVE 또는 INACTIVE여야 합니다.")
        String status
) {
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
