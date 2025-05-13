package roomescape.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ThemeCreateDto(

        @NotBlank
        @NotNull
        String name,

        @NotBlank
        @NotNull
        String description,

        @NotBlank
        @NotNull
        String thumbnail
) {
}
