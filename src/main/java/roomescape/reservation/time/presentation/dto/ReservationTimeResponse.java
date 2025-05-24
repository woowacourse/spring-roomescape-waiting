package roomescape.reservation.time.presentation.dto;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import roomescape.reservation.time.domain.ReservationTime;

public class ReservationTimeResponse {
    private Long id;
    private LocalTime startAt;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private ReservationTimeResponse() {
    }

    public ReservationTimeResponse(final ReservationTime reservationTime) {
        this.id = reservationTime.getId();
        this.startAt = reservationTime.getStartAt();
    }

    public Long getId() {
        return id;
    }

    public String getStartAt() {
        return startAt.format(TIME_FORMATTER);
    }
}
