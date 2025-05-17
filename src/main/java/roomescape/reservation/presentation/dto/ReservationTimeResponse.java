package roomescape.reservation.presentation.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public class ReservationTimeResponse {
    private Long id;
    private LocalTime startAt;

    private ReservationTimeResponse() {
    }

    public ReservationTimeResponse(final ReservationTime reservationTime) {
        this.id = reservationTime.getId();
        this.startAt = reservationTime.getStartAt();
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
