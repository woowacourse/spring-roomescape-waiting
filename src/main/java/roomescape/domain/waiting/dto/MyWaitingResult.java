package roomescape.domain.waiting.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyWaitingResult(
    Long id,
    String name,
    LocalDate date,
    LocalTime time,
    String themeName,
    int waitingNumber
) {

}
