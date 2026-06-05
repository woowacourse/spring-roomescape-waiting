package roomescape.reservationtime.application.dto.response;

import roomescape.reservationtime.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;

public record ReservationTimeFindResponse(
        Long id,
        LocalTime startAt
) {
    public static List<ReservationTimeFindResponse> from(List<ReservationTime> reservationTimes) {
        return reservationTimes.stream()
                .map(reservationTime -> new ReservationTimeFindResponse(
                        reservationTime.id(),
                        reservationTime.startAt()
                ))
                .toList();
    }
}
