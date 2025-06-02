package roomescape.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(Long id, String memberName, String themeName, LocalDate date, LocalTime startAt) {
}
