package roomescape.service.param;

import java.time.LocalDate;

public record CreateReservationParam(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
