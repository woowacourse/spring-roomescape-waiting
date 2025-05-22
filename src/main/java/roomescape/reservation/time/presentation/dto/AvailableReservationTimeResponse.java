package roomescape.reservation.time.presentation.dto;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import roomescape.reservation.time.domain.ReservationTime;

public class AvailableReservationTimeResponse {
    private Long id;
    private LocalTime startAt;
    private boolean alreadyBooked;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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

    public String getStartAt() {
        return startAt.format(TIME_FORMATTER);
    }

    public boolean isAlreadyBooked() {
        return alreadyBooked;
    }
}
