package roomescape.reservationtime.dto.response;

import roomescape.reservationtime.ReservationTime;

import java.time.LocalTime;
import java.util.List;

public record ReservationTimeFindResponse(
        Long id,
        LocalTime startAt
) {
    public static List<ReservationTimeFindResponse> from(List<ReservationTime> reservationTimes) {
        return reservationTimes.stream()
                .map(reservationTime -> new ReservationTimeFindResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt()
                ))
                .toList();
    }
}
