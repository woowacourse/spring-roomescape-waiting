package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminThemeRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        String description,
        String image
) {
}
