package roomescape.service.param;

import java.time.LocalDate;

public record CreateBookingParam(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
