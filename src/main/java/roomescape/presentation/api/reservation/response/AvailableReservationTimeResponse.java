package roomescape.presentation.api.reservation.response;

import java.time.LocalTime;
import roomescape.application.reservation.query.dto.AvailableReservationTimeResult;

public record AvailableReservationTimeResponse(
        Long timeId,
        LocalTime startAt,
        boolean booked
) {

    public static AvailableReservationTimeResponse from(AvailableReservationTimeResult availableReservationTimeResult) {
        return new AvailableReservationTimeResponse(
                availableReservationTimeResult.timeId(),
                availableReservationTimeResult.startAt(),
                availableReservationTimeResult.booked()
        );
    }
}
