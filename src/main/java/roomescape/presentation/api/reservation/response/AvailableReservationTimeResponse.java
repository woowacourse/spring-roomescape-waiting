package roomescape.presentation.api.reservation.response;

import roomescape.application.reservation.query.dto.AvailableReservationTimeResult;

public record AvailableReservationTimeResponse(
        Long timeId,
        String startAt,
        boolean booked
) {

    public static AvailableReservationTimeResponse from(AvailableReservationTimeResult availableReservationTimeResult) {
        return new AvailableReservationTimeResponse(
                availableReservationTimeResult.timeId(),
                ReservationDateTimeFormat.TIME.format(availableReservationTimeResult.startAt()),
                availableReservationTimeResult.booked()
        );
    }
}
