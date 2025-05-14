package roomescape.reservation.ui.dto.response;

import roomescape.reservation.domain.ReservationStatus;

public record ReservationStatusResponse(String id, String name) {

    public static ReservationStatusResponse from(final ReservationStatus status) {
        return new ReservationStatusResponse(status.name(), status.getDescription());
    }
}
