package roomescape.theme.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ThemeCreateRequest(
        @NotBlank(message = "이름을 입력해야 합니다.")
        String name,
        @NotBlank(message = "테마 설명을 입력해야 합니다.")
        String description,
        @NotBlank(message = "썸네일 URL을 입력해야 합니다.")
        String thumbnailUrl,
        @NotNull(message = "테마 가격을 입력해야 합니다.")
        @Positive(message = "테마 가격은 양수여야 합니다.")
        Integer price
) {
}
