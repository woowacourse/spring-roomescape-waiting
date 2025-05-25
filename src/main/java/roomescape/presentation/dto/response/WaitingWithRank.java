package roomescape.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingWithRank(
        String themeName,
        LocalDate date,
        LocalTime time,
        long rank
) {
}
