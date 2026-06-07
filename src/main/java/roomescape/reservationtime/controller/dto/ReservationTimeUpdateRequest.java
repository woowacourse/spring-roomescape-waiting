package roomescape.reservationtime.controller.dto;

import jakarta.validation.constraints.NotNull;
import roomescape.global.dto.ActivationStatus;

public record ReservationTimeUpdateRequest(
        @NotNull(message = "예약 시간 상태는 비어 있을 수 없습니다.")
        ActivationStatus status
) {
}
