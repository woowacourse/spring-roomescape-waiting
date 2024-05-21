package roomescape.dto;

import java.time.LocalDate;

//Todo DTO 분리?
public record ReservationWaitingResponse(long id, String name, LocalDate date, ReservationTimeResponse time,
                                         ThemeResponse theme, Integer priority) {
}
