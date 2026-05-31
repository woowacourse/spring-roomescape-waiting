package roomescape.dto;

import jakarta.validation.constraints.NotBlank;

public record ThemeRequest(
        @NotBlank(message = "테마 이름은 비어 있을 수 없습니다.")
        String name,

        @NotBlank(message = "설명은 비어 있을 수 없습니다")
        String description,

        @NotBlank(message = "썸네일 이미지는 비어 있을 수 없습니다.")
        String thumbnailImageUrl
) {
}
