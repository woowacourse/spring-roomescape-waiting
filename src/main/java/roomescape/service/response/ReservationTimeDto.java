package roomescape.service.response;

import java.time.LocalTime;

import roomescape.domain.ReservationTime;

public record ReservationTimeDto(Long id, LocalTime startAt) {

    public static ReservationTimeDto from(ReservationTime reservationTime) {
        return new ReservationTimeDto(reservationTime.getId(), reservationTime.getStartAt());
    }
}
