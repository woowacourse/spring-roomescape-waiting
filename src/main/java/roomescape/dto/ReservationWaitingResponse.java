package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationWaitingResponse(long id, String name, LocalDate date, ReservationTimeResponse time,
                                         ThemeResponse theme, Integer priority) {
    public String themeName() {
        return theme.name();
    }

    public LocalTime startAt() {
        return time.startAt();
    }
}
