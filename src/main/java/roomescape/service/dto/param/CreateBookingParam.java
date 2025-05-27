package roomescape.service.dto.param;

import java.time.LocalDate;

public record CreateBookingParam(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
