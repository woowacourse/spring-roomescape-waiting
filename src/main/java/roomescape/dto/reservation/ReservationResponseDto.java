package roomescape.dto.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponseDto(Long id, LocalTime time, LocalDate date, String themeName) {

}
