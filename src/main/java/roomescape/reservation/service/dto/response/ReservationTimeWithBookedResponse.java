package roomescape.reservation.service.dto.response;

import roomescape.reservation.repository.dto.ReservationTimeWithBooked;

public record ReservationTimeWithBookedResponse(
        ReservationTimeResponse timeResponse,
        boolean alreadyBooked
) {
    public static ReservationTimeWithBookedResponse from(ReservationTimeWithBooked dataResponse) {
        return new ReservationTimeWithBookedResponse(
                ReservationTimeResponse.from(dataResponse.time()),
                dataResponse.booked()
        );
    }
}
