package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationAvailabilityTimeRequest(

        @NotNull(message = "예약 가능 시간을 조회할 날짜가 입력되지 않았습니다.")
        LocalDate date,
        @NotNull
        Long themeId
) {
}
