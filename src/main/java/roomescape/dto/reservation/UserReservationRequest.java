package roomescape.dto.reservation;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record UserReservationRequest(
        @NotNull(message = "[ERROR] 날짜가 없습니다.") LocalDate date,
        @NotNull(message = "[ERROR] 예약시간 id가 없습니다.")
        @PositiveOrZero(message = "[ERROR] id는 양수여야 합니다.") Long timeId,
        @NotNull(message = "[ERROR] 테마 id가 없습니다.")
        @PositiveOrZero(message = "[ERROR] id는 양수여야 합니다.") Long themeId
) {
}
