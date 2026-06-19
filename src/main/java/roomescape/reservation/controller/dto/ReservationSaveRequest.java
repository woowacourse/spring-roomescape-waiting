package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;

public record ReservationSaveRequest(
        @NotBlank String name,
        @NotNull Long themeId,
        @NotNull Long timeId
) {

    public ReservationSaveServiceRequest toServiceDto() {
        return new ReservationSaveServiceRequest(name, themeId, timeId, null);
    }
}
