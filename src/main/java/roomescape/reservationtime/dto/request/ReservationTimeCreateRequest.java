package roomescape.reservationtime.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeCreateRequest(
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {
    public ReservationTimeCreateRequest {
        if (startAt == null) {
            throw new IllegalArgumentException("시작 시간은 null일 수 없습니다.");
        }
    }

    public ReservationTime toReservationTime() {
        return ReservationTime.withUnassignedId(startAt);
    }
}
