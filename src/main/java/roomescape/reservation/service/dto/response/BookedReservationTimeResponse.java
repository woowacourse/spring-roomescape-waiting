package roomescape.reservation.service.dto.response;

import roomescape.reservation.repository.dto.ReservationTimeWithBooked;

public record BookedReservationTimeResponse(
        ReservationTimeResponse timeResponse,
        boolean alreadyBooked
) {
    public static BookedReservationTimeResponse from(ReservationTimeWithBooked dataResponse) {
        return new BookedReservationTimeResponse(
                ReservationTimeResponse.from(dataResponse.time()),
                dataResponse.booked()
        );
    }
}
