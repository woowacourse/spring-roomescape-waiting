package roomescape.theme.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ThemeRequest(
        @NotBlank String name,
        String description,
        String thumbnail
) {
}
