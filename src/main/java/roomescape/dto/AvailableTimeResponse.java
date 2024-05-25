package roomescape.dto;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record AvailableTimeResponse(long id, LocalTime startAt, boolean isBooked) {
    public static AvailableTimeResponse of(ReservationTime reservationTime, boolean isBooked) {
        return new AvailableTimeResponse(reservationTime.getId(), reservationTime.getStartAt(), isBooked);
    }
}
