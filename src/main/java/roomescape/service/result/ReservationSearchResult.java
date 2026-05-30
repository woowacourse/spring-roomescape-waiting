package roomescape.service.result;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationSearchResult(
        long id,
        String name,
        LocalDate date,
        LocalTime startAt,
        String themeName,
        String status,
        Integer waitingRank
) {
}
