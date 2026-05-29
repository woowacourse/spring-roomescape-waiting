package roomescape.reservationTime.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ReservationTimeCreateRequest(
        @NotNull(message = "예약 시간을 입력해 주세요.")
        LocalTime startAt
) {
}
