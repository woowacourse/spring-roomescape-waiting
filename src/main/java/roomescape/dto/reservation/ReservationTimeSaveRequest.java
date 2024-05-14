package roomescape.dto.reservation;

import roomescape.domain.reservation.ReservationTime;

public record ReservationTimeSaveRequest(String startAt) {

    public ReservationTime toModel() {
        return new ReservationTime(startAt);
    }
}
