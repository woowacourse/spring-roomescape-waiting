package roomescape.domain.reservationtime;

import java.time.LocalTime;

public record AvailableReservationTimeDto(Long id, LocalTime startAt, boolean alreadyBooked) {
}
