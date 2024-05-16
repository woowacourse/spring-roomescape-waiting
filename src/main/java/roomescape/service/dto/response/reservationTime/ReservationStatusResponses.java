package roomescape.service.dto.response.reservationTime;

import java.util.List;
import roomescape.domain.ReservationStatus;

public record ReservationStatusResponses(List<ReservationStatusResponse> reservationStatuses) {

    public static ReservationStatusResponses from(ReservationStatus reservationStatus) {
        List<ReservationStatusResponse> responses = reservationStatus.getReservationStatus()
                .keySet()
                .stream()
                .map(reservationTime -> new ReservationStatusResponse(
                        reservationTime,
                        reservationStatus.findReservationStatusBy(reservationTime))
                ).toList();
        return new ReservationStatusResponses(responses);
    }
}
