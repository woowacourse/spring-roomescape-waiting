package roomescape.theme.dto;

import jakarta.validation.constraints.NotNull;

import roomescape.theme.domain.Theme;

public record ThemeRequest(
        @NotNull(message = "이름이 입력되지 않았습니다.")
        String name,
        String description,
        String thumbnail
) {
    public Theme createTheme() {
        return new Theme(name, description, thumbnail);
    }
}
