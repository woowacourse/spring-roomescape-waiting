package roomescape.service.theme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.theme.Theme;

public record ThemeRequest(
        @NotBlank(message = "이름을 입력해주세요") String name,
        @NotNull String description,
        @NotNull String thumbnail
) {
    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
