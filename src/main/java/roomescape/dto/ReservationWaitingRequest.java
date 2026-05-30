package roomescape.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationWaitingRequest(
        @NotBlank
        String name,
        @NotNull
        Long reservationId
) {
}
