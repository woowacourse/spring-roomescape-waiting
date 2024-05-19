package roomescape.service.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponse(
        Long id,
        String name,
        String theme,
        LocalDate date,
        LocalTime startAt
) {
}
