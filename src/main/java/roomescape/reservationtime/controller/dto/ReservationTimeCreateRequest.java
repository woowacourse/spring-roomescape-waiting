package roomescape.reservationtime.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ReservationTimeCreateRequest(
        @NotNull(message = "예약 시간은 비어 있을 수 없습니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {
}
