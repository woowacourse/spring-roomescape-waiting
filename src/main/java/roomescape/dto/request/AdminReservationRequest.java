package roomescape.dto.request;

import java.time.LocalDate;

public record AdminReservationRequest(
        Long userId,
        Long themeId,
        LocalDate date,
        Long timeId
) {

}
