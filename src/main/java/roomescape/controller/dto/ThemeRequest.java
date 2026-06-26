package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.service.dto.ThemeCreateCommand;

public record ThemeRequest(
        @NotBlank(message = "테마 이름은 필수입니다") String name,
        @NotBlank(message = "테마 설명은 필수입니다") String description,
        @NotBlank(message = "테마 썸네일은 필수입니다") String thumbnailUrl
) {
    public ThemeCreateCommand toCommand() {
        return new ThemeCreateCommand(name, description, thumbnailUrl);
    }
}
