package roomescape.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ThemeCreateRequest(
        @NotBlank(message = "테마 이름은 필수입니다.") String name,
        @NotBlank(message = "테마 설명은 필수입니다.") String description,
        @NotBlank(message = "테마 썸네일은 필수입니다.") String thumbnail
) {
}
