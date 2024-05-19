package roomescape.time.dto;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public record AvailableTimeResponse(Long timeId, LocalTime startAt, boolean isReserved) {

    public AvailableTimeResponse(ReservationTime reservationTime, boolean isReserved) {
        this(reservationTime.getId(), reservationTime.getStartAt(), isReserved);
    }
}
