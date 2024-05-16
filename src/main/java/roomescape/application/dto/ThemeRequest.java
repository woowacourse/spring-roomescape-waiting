package roomescape.application.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.Theme;
import roomescape.domain.ThemeName;

public record ThemeRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String thumbnail
) {

    public Theme toTheme() {
        return new Theme(new ThemeName(name), description, thumbnail);
    }
}
