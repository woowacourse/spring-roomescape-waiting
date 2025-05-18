package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.time.ReservationTime;

public record ReservationCreateCommand(LocalDate date, long memberId, Long timeId, Long themeId) {

    public Reservation convertToReservation(final Member member, final ReservationTime reservationTime,
                                            final Theme theme) {
        return new Reservation(null, member, date, reservationTime, theme);
    }
}
