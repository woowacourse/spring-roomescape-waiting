package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ThemeRequest(
        @Size(max = 255)
        @NotBlank(message = "255자 이하의 이름을 입력해주세요.")
        String name,
        @Size(max = 255)
        @NotBlank(message = "255자 이하의 설명을 입력해주세요.")
        String description,
        @NotBlank(message = "이미지 url을 입력해주세요.")
        String thumbnailUrl,
        @Positive(message = "가격은 0보다 커야 합니다.")
        int price
) {
}
