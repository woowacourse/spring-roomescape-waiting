package roomescape.service.param;

import java.time.LocalDate;

public record CreateWaitingParam(
        Long memberId,
         LocalDate date,
         Long timeId,
         Long themeId
) {
}
