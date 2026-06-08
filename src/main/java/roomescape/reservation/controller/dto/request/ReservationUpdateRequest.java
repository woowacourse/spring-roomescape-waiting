package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationUpdateRequest(
        @NotNull(message = "예약일을 입력해야 합니다.")
        LocalDate date,
        @NotNull(message = "예약 시간을 선택해야 합니다.")
        Long timeId
) {
}
