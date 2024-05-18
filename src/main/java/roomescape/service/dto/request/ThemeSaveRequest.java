package roomescape.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import roomescape.domain.Theme;

public record ThemeSaveRequest(
        @NotBlank
        @Size(max = 30)
        String name,

        @NotBlank
        @Size(max = 255)
        String description,

        @NotBlank
        @Size(max = 255)
        String thumbnail
) {

    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
