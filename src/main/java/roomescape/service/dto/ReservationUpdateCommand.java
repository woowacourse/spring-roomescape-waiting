package roomescape.service.dto;

import java.time.LocalDate;

public record ReservationUpdateCommand(
        Long id,
        String reserverName,
        LocalDate date,
        Long timeId
) {
}
