package roomescape.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank(message = "예약자 이름은 필수입니다.") String name,
        @NotNull(message = "날짜는 필수입니다.") LocalDate date,
        @NotNull(message = "예약 시간은 필수입니다.") @Positive(message = "예약 시간 ID는 양수여야 합니다.") Long timeId,
        @NotNull(message = "테마는 필수입니다.") @Positive(message = "테마 ID는 양수여야 합니다.") Long themeId
) {
}
