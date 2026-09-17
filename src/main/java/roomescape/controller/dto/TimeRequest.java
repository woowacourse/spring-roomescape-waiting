package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record TimeRequest(
        @NotNull
        LocalTime startAt
) { }
