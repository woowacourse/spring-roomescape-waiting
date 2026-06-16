package roomescape.domain.waiting.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.waiting.Waiting;

public record MyWaitingResult(
        Long id,
        String name,
        LocalDate date,
        LocalTime time,
        String themeName,
        int waitingNumber
) {

    public static MyWaitingResult of(Waiting waiting, int waitingNumber) {
        return new MyWaitingResult(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getTheme().getName(),
                waitingNumber
        );
    }
}