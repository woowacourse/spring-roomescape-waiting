package roomescape.dto.request;

import java.time.LocalDate;

public record AdminReservationRequestDto(
        Long userId,
        Long themeId,
        LocalDate date,
        Long timeId
) {

}
