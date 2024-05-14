package roomescape.service.dto.reservation;

import roomescape.domain.reservation.ReservationTime;

public class ReservationTimeResponse {

    private final long id;
    private final String startAt;

    public ReservationTimeResponse(long id, String startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTimeResponse(ReservationTime reservationTime) {
        this(reservationTime.getId(), reservationTime.getStartAt().toString());
    }

    public long getId() {
        return id;
    }

    public String getStartAt() {
        return startAt;
    }
}
