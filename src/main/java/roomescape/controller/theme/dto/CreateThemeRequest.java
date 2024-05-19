package roomescape.controller.theme.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import roomescape.domain.Theme;

public record CreateThemeRequest(
        @NotEmpty
        @Size(max = 255)
        String name,

        @NotNull
        @Size(max = 255)
        String description,

        @NotNull
        @Size(max = 255)
        String thumbnail) {

    public Theme toDomain() {
        return new Theme(null, name, description, thumbnail);
    }
}
