package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReservationRescheduleDto(
        @NotNull(message = "newSlotId는 필수 입력값입니다.")
        Long newSlotId
) {
}
