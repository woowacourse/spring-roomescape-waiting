package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AdminThemeRequest(
        @NotBlank(message = "테마 이름은 필수입니다.") String name,
        @NotBlank(message = "테마 설명은 필수입니다.") String description,
        @NotBlank(message = "테마 이미지 URL은 필수입니다.") String imageUrl,
        @Positive(message = "테마 가격은 0보다 커야 합니다.") int price
) {
}