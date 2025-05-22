package roomescape.reservation.service.dto;

import roomescape.member.service.dto.MemberInfo;
import roomescape.reservation.domain.Waiting;

public record WaitingInfo(long id, ReservationTimeInfo timeInfo, ThemeInfo themeInfo, MemberInfo memberInfo) {
    public WaitingInfo(Waiting waiting) {
        this(waiting.getId(),
                new ReservationTimeInfo(waiting.getTime()),
                new ThemeInfo(waiting.getTheme()),
                new MemberInfo(waiting.getMember())
        );
    }
}
