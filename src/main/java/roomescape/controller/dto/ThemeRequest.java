package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ThemeRequest(
        @NotBlank
        String name,

        @NotBlank
        String description,

        @NotBlank
        String thumbnailUrl,

        @NotNull @Positive
        Long price
) { }
