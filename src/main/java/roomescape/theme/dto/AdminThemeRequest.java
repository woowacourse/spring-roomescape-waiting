package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminThemeRequest(
        @NotBlank(message = "테마 이름은 필수로 입력해야 합니다.")
        String name,
        String description,
        String image
) {
}
