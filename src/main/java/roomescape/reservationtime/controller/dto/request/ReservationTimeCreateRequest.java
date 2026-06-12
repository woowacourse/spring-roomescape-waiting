package roomescape.reservationtime.controller.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ReservationTimeCreateRequest(
        @NotNull(message = "예약 시작 시간을 입력해야 합니다.")
        LocalTime startAt
) {
}
