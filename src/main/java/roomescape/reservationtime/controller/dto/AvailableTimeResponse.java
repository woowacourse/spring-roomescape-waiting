package roomescape.reservationtime.controller.dto;

import java.time.LocalTime;
import roomescape.reservationtime.repository.dto.ReservationTimeAvailability;

public record AvailableTimeResponse(
        Long id,
        LocalTime startAt,
        boolean isAvailable
) {

    public static AvailableTimeResponse from(ReservationTimeAvailability timeAvailability) {
        return new AvailableTimeResponse(
                timeAvailability.getReservationTime().getId(),
                timeAvailability.getReservationTime().getStartAt(),
                timeAvailability.isAvailable());
    }
}
