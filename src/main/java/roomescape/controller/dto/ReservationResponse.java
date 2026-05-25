package roomescape.controller.dto;

import java.time.LocalDate;

public record ReservationResponse(long id, String name, LocalDate date, TimeResponse time, ThemeResponse theme) {
}
