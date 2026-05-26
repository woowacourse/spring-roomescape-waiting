package roomescape.theme.controller.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.theme.service.dto.ThemeSaveServiceDto;

public record ThemeSaveRequestDto(@NotBlank String name, @NotBlank String description, String imageUrl) {

    public ThemeSaveServiceDto toServiceDto() {
        return new ThemeSaveServiceDto(name, description, imageUrl);
    }
}
