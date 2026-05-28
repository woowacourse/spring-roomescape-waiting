package roomescape.dto.reservationtime;

import roomescape.domain.reservationtime.AvailableReservationTime;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(Long id, LocalTime startAt, Boolean isAvailable) {

    public static AvailableReservationTimeResponse from(AvailableReservationTime availableReservationTime) {
        return new AvailableReservationTimeResponse(availableReservationTime.getId(), availableReservationTime.getStartAt(), availableReservationTime.isAvailable());
    }
}
