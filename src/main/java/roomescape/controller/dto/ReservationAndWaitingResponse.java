package roomescape.controller.dto;

import java.time.LocalDate;

public record ReservationAndWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme,
        boolean isReserved,
        Integer waitingNumber
) {
}
