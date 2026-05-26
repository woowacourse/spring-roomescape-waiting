package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ThemeCreateRequest(
        @NotBlank(message = "테마 이름은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "테마 이름은 255자를 넘을 수 없습니다.")
        String name,

        @NotBlank(message = "테마 설명은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "테마 설명은 255자를 넘을 수 없습니다.")
        String description,

        @NotBlank(message = "테마 썸네일은 비어 있을 수 없습니다.")
        @Size(max = 2048, message = "테마 썸네일은 2048자를 넘을 수 없습니다.")
        String thumbnail
) {
}
