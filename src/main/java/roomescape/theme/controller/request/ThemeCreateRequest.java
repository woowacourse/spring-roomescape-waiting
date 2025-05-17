package roomescape.theme.controller.request;

import jakarta.validation.constraints.NotBlank;

public record ThemeCreateRequest(
        @NotBlank(message = "테마명을 입력해주세요.") String name,
        @NotBlank(message = "테마 설명을 입력해주세요.") String description,
        @NotBlank(message = "썸네일을 입력해주세요.") String thumbnail
) {
}
