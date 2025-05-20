package roomescape.service.result;

import roomescape.domain.Waiting;

import java.time.LocalDate;

public record WaitingResult(
        Long id,
        MemberResult member,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        int order
) {
    public static WaitingResult from(final Waiting waiting) {
        return new WaitingResult(
                waiting.getId(),
                MemberResult.from(waiting.getMember()),
                waiting.getDate(),
                ReservationTimeResult.from(waiting.getTime()),
                ThemeResult.from(waiting.getTheme()),
                waiting.getOrder()
        );
    }
}
