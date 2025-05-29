package roomescape.waiting.service.dto;

import java.time.LocalDate;
import roomescape.member.service.dto.MemberInfo;
import roomescape.waiting.domain.Waiting;
import roomescape.reservation.service.dto.ReservationTimeInfo;
import roomescape.reservation.service.dto.ThemeInfo;

public record WaitingInfo(
        long id,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme,
        MemberInfo member,
        long order
) {

    public WaitingInfo(Waiting waiting) {
        this(waiting.getId(),
                waiting.getDate(),
                new ReservationTimeInfo(waiting.getTime()),
                new ThemeInfo(waiting.getTheme()),
                new MemberInfo(waiting.getMember()),
                waiting.getPriority()
        );
    }
}
