package roomescape.service.response;

import java.time.LocalTime;

import roomescape.domain.ReservationTime;

public record BookableReservationTimeDto(Long id, LocalTime startAt, boolean alreadyBooked) {

    public static BookableReservationTimeDto of(ReservationTime reservationTime, boolean alreadyBooked) {
        return new BookableReservationTimeDto(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked);
    }
}
