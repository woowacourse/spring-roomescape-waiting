package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationStatusUpdateDto(
        @NotNull(message = "status는 필수 입력입니다.")
        ReservationStatus status
) {
}
