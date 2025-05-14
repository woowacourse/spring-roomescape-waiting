package roomescape.model;

import java.time.LocalTime;

public class AvailableReservationTime extends ReservationTime {

    private final Boolean alreadyBooked;

    public AvailableReservationTime(Long id, LocalTime startAt, Boolean alreadyBooked) {
        super(id, startAt);
        this.alreadyBooked = alreadyBooked;
    }

    public Boolean getAlreadyBooked() {
        return alreadyBooked;
    }
}
