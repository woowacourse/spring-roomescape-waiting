package roomescape.service.dto.result;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Waiting;

public record WaitingResult(
        Long id,
        MemberResult waiter,
        ThemeResult theme,
        LocalDate date,
        ReservationTimeResult time
) {
    public static WaitingResult from(Waiting waiting) {
        return new WaitingResult(
                waiting.getId(),
                MemberResult.from(waiting.getMember()),
                ThemeResult.from(waiting.getTheme()),
                waiting.getDate(),
                ReservationTimeResult.from(waiting.getTime())
        );
    }

    public static List<WaitingResult> from(List<Waiting> waitings) {
        return waitings.stream()
                .map(WaitingResult::from)
                .toList();
    }
}
