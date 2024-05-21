package roomescape.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRankResponse(long id, String name, LocalDate date, LocalTime time, long rank) {
}
