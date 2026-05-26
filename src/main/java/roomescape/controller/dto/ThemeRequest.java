package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ThemeRequest(
        @NotBlank(message = "name은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "name은 255자를 넘을 수 없습니다.")
        String name,
        @Size(max = 255, message = "description은 255자를 넘을 수 없습니다.")
        String description,
        @Size(max = 255, message = "thumbnail은 255자를 넘을 수 없습니다.")
        String thumbnail
) {
}
