package roomescape.reservation.service.dto;

import java.time.LocalDate;
import roomescape.member.service.dto.MemberInfo;
import roomescape.reservation.domain.Waiting;

public record WaitingInfo(long id, LocalDate date, ReservationTimeInfo timeInfo, ThemeInfo themeInfo, MemberInfo memberInfo) {
    public WaitingInfo(Waiting waiting) {
        this(waiting.getId(),
                waiting.getDate(),
                new ReservationTimeInfo(waiting.getTime()),
                new ThemeInfo(waiting.getTheme()),
                new MemberInfo(waiting.getMember())
        );
    }
}
