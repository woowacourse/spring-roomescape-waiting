package roomescape.reservation.presentation.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public class AvailableReservationTimeResponse {
    private Long id;
    private LocalTime startAt;
    private boolean alreadyBooked;

    private AvailableReservationTimeResponse() {
    }

    public AvailableReservationTimeResponse(final ReservationTime reservationTime, final boolean alreadyBooked) {
        this.id = reservationTime.getId();
        this.startAt = reservationTime.getStartAt();
        this.alreadyBooked = alreadyBooked;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isAlreadyBooked() {
        return alreadyBooked;
    }
}
