package roomescape.controller.time.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AvailabilityTimeRequest(
        @NotNull
        LocalDate date,

        @NotNull
        Long themeId) {
}
