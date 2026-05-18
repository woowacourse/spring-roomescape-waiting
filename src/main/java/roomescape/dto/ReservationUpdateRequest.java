package roomescape.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ReservationUpdateRequest(
        @NotNull(message = "날짜를 입력해주세요.")
        LocalDate targetDate,
        @NotNull(message = "시간을 입력해주세요.")
        long timeId
) {
}
