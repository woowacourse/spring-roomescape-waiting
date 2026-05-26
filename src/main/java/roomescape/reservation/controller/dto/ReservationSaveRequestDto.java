package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import roomescape.reservation.service.dto.ReservationSaveServiceDto;

public record ReservationSaveRequestDto(
        @NotBlank String name,
        @NotNull Long themeId,
        @NotNull Long timeId
) {

    public ReservationSaveServiceDto toServiceDto() {
        return new ReservationSaveServiceDto(name, themeId, timeId);
    }
}
