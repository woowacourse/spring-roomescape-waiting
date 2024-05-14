package roomescape.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.reservation.domain.Theme;

public record ThemeCreateRequest(
        @NotNull @NotBlank String name,
        @NotNull @NotBlank String description,
        @NotNull @NotBlank String thumbnail
) {

    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
