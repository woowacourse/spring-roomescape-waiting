package roomescape.reservationTime.presentation.dto;

import java.time.LocalTime;

public record ReservationTimeRequest(LocalTime startAt) {
    public ReservationTimeRequest {
        if (startAt == null) {
            throw new IllegalArgumentException("time은 null 일 수 없습니다.");
        }
    }
}
