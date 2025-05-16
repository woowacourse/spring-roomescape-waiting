package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeResponseDto(Long id, LocalTime startAt) {

    public static ReservationTimeResponseDto of(ReservationTime reservationTime) {
        return new ReservationTimeResponseDto(reservationTime.getId(), reservationTime.getStartAt());
    }
}
