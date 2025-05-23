package roomescape.dto.waiting;

import java.time.LocalDate;

public record MemberWaitingCreateRequest(
        LocalDate date,
        Long themeId,
        Long timeId
) {
}
