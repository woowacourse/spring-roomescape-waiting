package roomescape.theme.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.theme.Theme;

public record ThemeSaveRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String thumbnailUrl
) {
    public Theme toDomain() {
        return new Theme(
                null,
                this.name,
                this.description,
                this.thumbnailUrl
        );
    }
}
