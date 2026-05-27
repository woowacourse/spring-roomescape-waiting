package roomescape.controller.dto;

import java.time.LocalDate;

public record ReservationAndWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse timeResponse,
        ThemeResponse themeResponse,
        boolean isReserved,
        Integer waitingNumber
) {
}
