package roomescape.dto.response;

import roomescape.domain.reservation.ReservationStatus;

public record ReservationStatusResponse(String name) {

    public static ReservationStatusResponse from(ReservationStatus status) {
        if (status == ReservationStatus.RESERVED) {
            return new ReservationStatusResponse("예약");
        }

        return new ReservationStatusResponse(status.getName());
    }
}
