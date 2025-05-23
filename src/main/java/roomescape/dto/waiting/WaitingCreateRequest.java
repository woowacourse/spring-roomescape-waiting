package roomescape.dto.waiting;

import java.time.LocalDate;

public record WaitingCreateRequest(
        LocalDate date,
        Long memberId,
        Long themeId,
        Long timeId
) {
}
