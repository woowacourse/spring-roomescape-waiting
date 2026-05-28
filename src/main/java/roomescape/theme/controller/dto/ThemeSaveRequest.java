package roomescape.theme.controller.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.theme.service.dto.ThemeSaveServiceRequest;

public record ThemeSaveRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String imageUrl) {

    public ThemeSaveServiceRequest toServiceDto() {
        return new ThemeSaveServiceRequest(name, description, imageUrl);
    }
}
