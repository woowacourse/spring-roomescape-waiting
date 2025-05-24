package roomescape.reservation.application.waiting.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.waiting.ReservationWaiting;

public record ReservationWaitingCreateCommand(LocalDate date, long memberId, long timeId, long themeId) {

    public ReservationWaiting convertToEntity(final Reservation reservation, final Member member) {
        return new ReservationWaiting(reservation, member);
    }
}
