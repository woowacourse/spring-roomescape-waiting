package roomescape.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

public record ThemeSaveRequest(
        @NotNull @NotBlank String name,
        @NotNull @NotBlank String description,
        @NotNull @NotBlank String thumbnail
) {

    public Theme toTheme() {
        return new Theme(new ThemeName(name), new Description(description), thumbnail);
    }
}
