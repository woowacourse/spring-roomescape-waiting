package roomescape.dto.request;

import java.time.LocalDate;

public record WaitingCreationRequest(
        LocalDate date,
        Long themeId,
        Long timeId
) {

}
