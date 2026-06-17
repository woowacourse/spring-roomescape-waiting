package roomescape.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ThemeCreateCommand(
        @NotNull(message = "테마 이름은 비워둘 수 없습니다.")
        @NotBlank(message = "테마 이름은 비워둘 수 없습니다.")
        String name,

        @NotNull(message = "테마 설명은 비워둘 수 없습니다.")
        @NotBlank(message = "테마 설명은 비워둘 수 없습니다.")
        String description,

        @NotNull(message = "테마 썸네일 URL은 비워둘 수 없습니다.")
        @NotBlank(message = "테마 썸네일 URL은 비워둘 수 없습니다.")
        String thumbnailUrl
) {
}
