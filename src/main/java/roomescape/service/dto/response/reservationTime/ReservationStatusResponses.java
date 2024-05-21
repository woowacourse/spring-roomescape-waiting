package roomescape.service.dto.response.reservationTime;

import java.util.List;
import roomescape.domain.reservation.ReservationStatuses;

public record ReservationStatusResponses(List<ReservationStatusResponse> reservationStatuses) {

    public static ReservationStatusResponses from(ReservationStatuses reservationStatuses) {
        List<ReservationStatusResponse> responses = reservationStatuses.getReservationStatuses()
                .stream()
                .map(status -> new ReservationStatusResponse(status.getTime(), status.isBooked()))
                .toList();
        return new ReservationStatusResponses(responses);
    }
}
