package roomescape.domain.reservationtime;

import java.time.LocalTime;

public class ReservationTimeResponse {

    private final Long id;
    private final LocalTime startAt;

    private ReservationTimeResponse(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
