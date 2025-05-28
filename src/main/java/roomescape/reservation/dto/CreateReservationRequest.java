package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateReservationRequest(
        @NotNull(message = "날짜를 입력해주세요.") LocalDate date,
        long timeId,
        long themeId
) {
}
