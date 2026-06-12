package roomescape.theme.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ThemeCreateRequest(
        @NotBlank(message = "이름을 입력해야 합니다.")
        String name,
        @NotBlank(message = "테마 설명을 입력해야 합니다.")
        String description,
        @NotBlank(message = "썸네일 URL을 입력해야 합니다.")
        String thumbnailUrl
) {
}
