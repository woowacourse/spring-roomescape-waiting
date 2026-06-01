package roomescape.reservationTime.presentation.dto.response;

import roomescape.reservationTime.domain.ReservationTime;

public record ReservationTimeResponse(
        Long id,
        String startAt,
        boolean alreadyBooked
) {
    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return from(reservationTime, false);
    }

    public static ReservationTimeResponse from(ReservationTime reservationTime, boolean alreadyBooked) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt().toString(),
                alreadyBooked
        );
    }
}
