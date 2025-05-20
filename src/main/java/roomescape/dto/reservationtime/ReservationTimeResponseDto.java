package roomescape.dto.reservationtime;

import java.time.LocalTime;

public record ReservationTimeResponseDto(Long id, LocalTime startAt) {

}
