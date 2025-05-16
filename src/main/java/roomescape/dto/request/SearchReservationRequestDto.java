package roomescape.dto.request;

import java.time.LocalDate;

public record SearchReservationRequestDto(
        Long userId,
        Long themeId,
        LocalDate from,
        LocalDate to
) {

}
