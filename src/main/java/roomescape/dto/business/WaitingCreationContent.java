package roomescape.dto.business;

import java.time.LocalDate;

public record WaitingCreationContent(
        LocalDate date,
        Long themeId,
        Long timeId,
        Long memberId
) {

}
