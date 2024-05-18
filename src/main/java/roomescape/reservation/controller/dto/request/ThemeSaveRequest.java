package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.reservation.domain.Theme;

public record ThemeSaveRequest(
        @NotNull
        String name,

        @NotNull
        String description,

        @NotNull
        String thumbnail
) {
    public Theme toEntity() {
        return new Theme(name, description, thumbnail);
    }
}
