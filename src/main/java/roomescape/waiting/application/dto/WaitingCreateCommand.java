package roomescape.waiting.application.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record WaitingCreateCommand(LocalDate date, long timeId, long themeId, long memberId) {

    public Waiting convertToEntity(final Reservation reservation, final Member member) {
        return new Waiting(reservation, member);
    }
}
