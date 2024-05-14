package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ThemeRequest(
        @NotBlank(message = "이름을 입력해주세요") String name,
        @NotNull String description,
        @NotNull String thumbnail
) {
}
