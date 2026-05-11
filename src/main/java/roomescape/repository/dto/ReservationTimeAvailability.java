package roomescape.repository.dto;


import lombok.Getter;
import roomescape.domain.ReservationTime;

@Getter
public class ReservationTimeAvailability {
    private final ReservationTime reservationTime;
    private final boolean isAvailable;


    private ReservationTimeAvailability(ReservationTime reservationTime, boolean isAvailable) {
        this.reservationTime = reservationTime;
        this.isAvailable = isAvailable;
    }

    public static ReservationTimeAvailability available(ReservationTime reservationTime) {
        return new ReservationTimeAvailability(reservationTime, true);
    }

    public static ReservationTimeAvailability unavailable(ReservationTime reservationTime) {
        return new ReservationTimeAvailability(reservationTime, false);
    }
}
