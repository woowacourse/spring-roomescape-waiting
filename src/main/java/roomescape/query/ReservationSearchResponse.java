package roomescape.query;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationSearchResponse(long id, String name, LocalDate date, LocalTime startAt, String theme) {
}
