package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.model.Theme;

public record ThemeRegisterDto(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String thumbnail
) {

    public Theme convertToTheme() {
        return new Theme(name, description, thumbnail);
    }
}
