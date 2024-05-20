package roomescape.controller.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.Status;

public record CreateReservationDto(
        @NotNull
        Long memberId,

        @NotNull
        Long themeId,

        @NotNull
        LocalDate date,

        @NotNull
        Long timeId,

        @NotNull
        Status status) {
}
