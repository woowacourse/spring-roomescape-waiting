package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record ThemeRequest(@NotBlank String name, @NotBlank String description, @NotBlank String thumbnail) {
}
