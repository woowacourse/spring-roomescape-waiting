package roomescape.service.result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.Waiting;

public record WaitingResult(
        Long id,
        String waiterName,
        String themeName,
        LocalDate date,
        LocalTime time
) {
    public static WaitingResult from(Waiting waiting) {
        return new WaitingResult(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt()
        );
    }

    public static List<WaitingResult> from(List<Waiting> waitings) {
        return waitings.stream()
                .map(WaitingResult::from)
                .toList();
    }
}
