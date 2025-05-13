package roomescape.reservationTime.dto.request;

import java.time.LocalTime;

public record ReservationTimeRequest(LocalTime startAt) {
    public ReservationTimeRequest {
        if (startAt == null) {
            throw new IllegalArgumentException("시간은 null 일 수 없습니다.");
        }

    }
}
