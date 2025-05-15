package roomescape.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeCreateRequest(
        @NotNull(message = "[ERROR] 예약시간이 없습니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

    public ReservationTime createWithoutId() {
        return new ReservationTime(null, startAt);
    }
}
