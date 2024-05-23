package roomescape.controller.time.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record IsMineRequest(

        @NotNull
        LocalDate date,
        @NotNull
        Long themeId,
        @NotNull
        Long timeId) {
}
