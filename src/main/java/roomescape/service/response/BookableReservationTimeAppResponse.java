package roomescape.service.response;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record BookableReservationTimeAppResponse(Long id, LocalTime startAt, boolean alreadyBooked) {

    public static BookableReservationTimeAppResponse of(ReservationTime reservationTime, boolean alreadyBooked) {
        return new BookableReservationTimeAppResponse(
            reservationTime.getId(),
            reservationTime.getStartAt(),
            alreadyBooked);
    }
}
