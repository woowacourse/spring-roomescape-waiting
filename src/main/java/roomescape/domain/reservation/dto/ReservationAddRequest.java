package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

public record ReservationAddRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {

    public Reservation toEntity(ReservationTime reservationTime, Theme theme, Member member) {
        return new Reservation(null, date, reservationTime, theme, member);
    }
}
