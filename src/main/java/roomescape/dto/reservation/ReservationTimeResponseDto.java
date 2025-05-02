package roomescape.dto.reservation;

import java.time.LocalTime;

public record ReservationTimeResponseDto(Long id, LocalTime startAt) {

}
