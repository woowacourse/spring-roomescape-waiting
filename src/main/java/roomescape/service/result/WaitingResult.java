package roomescape.service.result;

import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.util.List;

public record WaitingResult(
        Long id,
        MemberResult member,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme
) {
    public static WaitingResult from(Waiting waiting) {
        return new WaitingResult(
                waiting.getId(),
                MemberResult.from(waiting.getMember()),
                waiting.getDate(),
                ReservationTimeResult.from(waiting.getTime()),
                ThemeResult.from(waiting.getTheme())
        );
    }

    public static List<WaitingResult> from(List<Waiting> waitings) {
        return waitings.stream()
                .map(WaitingResult::from)
                .toList();
    }
}
