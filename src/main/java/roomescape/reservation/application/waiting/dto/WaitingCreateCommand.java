package roomescape.reservation.application.waiting.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.timeslot.TimeSlot;
import roomescape.reservation.domain.waiting.Waiting;

public record WaitingCreateCommand(LocalDate date, long memberId, long timeId, long themeId) {

    public Waiting convertToEntity(final LocalDate date, final TimeSlot time, final Theme theme, final Member member) {
        return new Waiting(date, time, theme, member);
    }
}
