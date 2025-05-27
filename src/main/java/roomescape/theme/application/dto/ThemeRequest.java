package roomescape.theme.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ThemeRequest(
        @NotBlank(message = "테마 이름은 필수 입력값입니다.") String name,
        @NotBlank(message = "테마 설명은 필수 입력값입니다.") String description,
        @NotBlank(message = "썸네일은 필수 입력값입니다.") String thumbnail) {
}
