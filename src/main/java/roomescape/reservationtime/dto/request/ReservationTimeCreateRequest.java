package roomescape.reservationtime.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeCreateRequest(
    @NotNull(message = "예약 시간은 빈 값일 수 없습니다")
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt
) {
    public ReservationTime toReservationTime() {
        return new ReservationTime(null, startAt);
    }
}
