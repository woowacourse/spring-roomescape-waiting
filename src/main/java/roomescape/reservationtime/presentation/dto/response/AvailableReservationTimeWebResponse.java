package roomescape.reservationtime.presentation.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record AvailableReservationTimeWebResponse(
        Long timeId,
        LocalTime startAt,
        boolean alreadyBooked
) {
    public static AvailableReservationTimeWebResponse of(ReservationTime reservationTime, boolean alreadyBooked) {
        return new AvailableReservationTimeWebResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked
        );
    }
}
