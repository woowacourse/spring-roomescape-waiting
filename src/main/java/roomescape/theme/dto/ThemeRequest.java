package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;

public record ThemeRequest(
        @NotBlank(message = "이름은 비어 있을 수 없습니다.")
        String name,

        @NotBlank(message = "테마 설명은 비어 있을 수 없습니다.")
        String description,

        @NotBlank(message = "테마 이미지는 비어 있을 수 없습니다.")
        String imageUrl
) {

}
