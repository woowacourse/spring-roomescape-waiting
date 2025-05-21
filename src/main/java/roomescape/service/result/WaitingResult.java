package roomescape.service.result;

import roomescape.domain.Waiting;

import java.time.LocalDate;

public record WaitingResult(
        MemberResult member,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme
) {
    public static WaitingResult from(Waiting waiting) {
        return new WaitingResult(
                MemberResult.from(waiting.getMember()),
                waiting.getDate(),
                ReservationTimeResult.from(waiting.getTime()),
                ThemeResult.from(waiting.getTheme())
        );
    }
}
