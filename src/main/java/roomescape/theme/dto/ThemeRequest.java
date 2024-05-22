package roomescape.theme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ThemeRequest(
        @NotBlank(message = "테마의 이름은 null 또는 공백일 수 없습니다.")
        @Size(min = 1, max = 20, message = "테마의 이름은 1~20글자 사이여야 합니다.")
        String name,
        @NotBlank(message = "테마의 설명은 null 또는 공백일 수 없습니다.")
        @Size(min = 1, max = 100, message = "테마의 설명은 1~100글자 사이여야 합니다.")
        String description,
        @NotBlank(message = "테마의 쌈네일은 null 또는 공백일 수 없습니다.")
        String thumbnail
) {
}
