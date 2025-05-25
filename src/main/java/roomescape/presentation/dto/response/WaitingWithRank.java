package roomescape.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingWithRank(
        Long id,
        String themeName,
        LocalDate date,
        LocalTime time,
        long rank
) {
}
