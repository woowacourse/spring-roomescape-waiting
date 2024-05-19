package roomescape.controller.theme.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PopularThemeRequest(
        @NotNull
        LocalDate from,

        @NotNull
        LocalDate until,

        @NotNull
        @Positive
        Integer limit) {
}
