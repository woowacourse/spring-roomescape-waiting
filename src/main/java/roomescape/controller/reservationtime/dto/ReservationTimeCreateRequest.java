package roomescape.controller.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;

public record ReservationTimeCreateRequest(
        @NotNull(message = "예약 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {
}
