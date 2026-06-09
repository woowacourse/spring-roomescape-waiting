package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationUpdateRequest(
        @NotNull(message = "새로운 예약 날짜를 입력해주세요.")

        LocalDate date,
        @NotNull(message = "새로운 예약 시간을 선택해주세요.")
        Long timeId
) {
}
