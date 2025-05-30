package roomescape.waiting.application.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.waiting.domain.Waiting;

public record WaitingCreateCommand(LocalDate date, long memberId, long timeId, long themeId) {

    public Waiting convertToEntity(final LocalDate date, final TimeSlot time, final Theme theme, final Member member) {
        return new Waiting(date, time, theme, member);
    }
}
