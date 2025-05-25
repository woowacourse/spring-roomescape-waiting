package roomescape.reservation.service.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.waiting.domain.Waiting;

public record ReservationCreateFromWaitingCommand(Member member, Theme theme, LocalDate date, ReservationTime time) {

    public ReservationCreateFromWaitingCommand(final Waiting waiting) {
        this(waiting.getMember(), waiting.getTheme(), waiting.getDate(), waiting.getTime());
    }
}
