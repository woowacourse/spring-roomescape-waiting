package roomescape.dto.request;

import java.time.LocalDate;

public record ReservationCreationRequest(
        Long themeId,
        LocalDate date,
        Long timeId
) {

}
