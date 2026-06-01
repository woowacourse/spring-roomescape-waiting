package roomescape.controller.dto.response;

import java.time.LocalTime;
import roomescape.domain.reservation.ReservationTime;

public class ReservationTimeResponse {
    private final long id;
    private final LocalTime startAt;

    public ReservationTimeResponse(long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTimeResponse toDto(ReservationTime reservationTime) {
        return new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
