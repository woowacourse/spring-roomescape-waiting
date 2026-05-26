package roomescape.reservationwaiting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationWaitingRequest(
        @NotNull(message = "예약자 이름은 필수입니다.") String name,
        @NotNull(message = "예약은 필수입니다.") @Positive(message = "예약 ID는 양수여야 합니다.") Long reservationId
) {
}
