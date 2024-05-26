package roomescape.service.dto.response.reservationTime;

import java.util.List;
import roomescape.domain.reservationtime.ReservationTimeStatuses;

public record ReservationTimeStatusResponses(List<ReservationTimeStatusResponse> reservationStatuses) {

    public static ReservationTimeStatusResponses from(ReservationTimeStatuses reservationTimeStatuses) {
        List<ReservationTimeStatusResponse> responses = reservationTimeStatuses.getReservationTimeStatuses()
                .stream()
                .map(status -> new ReservationTimeStatusResponse(status.getTime(), status.isBooked()))
                .toList();
        return new ReservationTimeStatusResponses(responses);
    }
}
