package roomescape.reservationtime.service;

import lombok.Getter;
import roomescape.reservationtime.domain.ReservationTime;

@Getter
public class ReservationTimeAvailability {
    private final ReservationTime reservationTime;
    private final boolean isAvailable;

    public ReservationTimeAvailability(ReservationTime reservationTime, boolean isAvailable) {
        this.reservationTime = reservationTime;
        this.isAvailable = isAvailable;
    }
}
