package roomescape.theme.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.theme.entity.Theme;

public record ThemeCreateRequest(
        @NotNull String name,
        @NotNull String description,
        @NotNull String thumbnail
) {
    public Theme toEntity() {
        return new Theme(name, description, thumbnail);
    }
}
