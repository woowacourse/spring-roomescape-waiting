package roomescape.theme.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ThemeCreateRequest(
        @NotBlank(message = "테마 이름을 입력해 주세요.")
        String name,

        @NotBlank(message = "테마 설명을 입력해 주세요.")
        String description,

        @NotBlank(message = "테마 썸네일을 입력해 주세요.")
        String thumbnail
) {
}
