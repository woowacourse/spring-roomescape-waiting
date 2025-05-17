package roomescape.dto.reservationtime;

import java.time.LocalTime;

public record ReservationTimeSlotResponseDto(Long id, LocalTime time, boolean alreadyBooked) {

}
