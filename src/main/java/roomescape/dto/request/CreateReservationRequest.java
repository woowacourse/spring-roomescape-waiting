package roomescape.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateReservationRequest(
        @FutureOrPresent(message = "날짜는 현재보다 미래여야합니다.") LocalDate date,
        @NotNull(message = "예약 시간 ID는 필수입니다.") Long timeId,
        @NotNull(message = "테마 ID는 필수입니다.") Long themeId
) {

}
