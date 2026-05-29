package roomescape.reservationwaiting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationWaitingRequest(
        @NotNull(message = "날짜는 필수입니다.") LocalDate date,
        @NotNull(message = "시간은 필수입니다.") @Positive(message = "시간 ID는 양수여야 합니다.") Long timeId,
        @NotNull(message = "테마는 필수입니다.") @Positive(message = "테마 ID는 양수여야 합니다.") Long themeId
) {
}