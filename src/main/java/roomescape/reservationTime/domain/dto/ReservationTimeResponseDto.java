package roomescape.reservationTime.domain.dto;

import java.time.LocalTime;
import roomescape.reservationTime.domain.ReservationTime;

public record ReservationTimeResponseDto(Long id, LocalTime startAt) {

    public static ReservationTimeResponseDto of(ReservationTime reservationTime) {
        return new ReservationTimeResponseDto(reservationTime.getId(), reservationTime.getStartAt());
    }
}
