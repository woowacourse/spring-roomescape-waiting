package roomescape.dto;

import java.time.LocalDate;

public record ReservationWaitingResponse(long id, String name, LocalDate date, ReservationTimeResponse time,
                                         ThemeResponse theme, Integer priority) {
}
