package roomescape.controller.dto;

import java.time.LocalDate;

public record ReservationAndWaitingResponse(
        String name,
        LocalDate date,
        TimeResponse timeResponse,
        ThemeResponse themeResponse,
        boolean isReserved,
        Integer waitingNumber
) {
}
