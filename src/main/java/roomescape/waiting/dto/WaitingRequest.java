package roomescape.waiting.dto;

import java.time.LocalDate;

public record WaitingRequest(
        LocalDate date,
        long timeId,
        long themeId
) {
}
