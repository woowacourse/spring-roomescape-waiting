package roomescape.reservation.service.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public record ReservationCreateCommand(LocalDate date, long memberId, Long timeId, Long themeId) {

    public Reservation convertToReservation(final Member member, final ReservationTime reservationTime,
                                            final Theme theme) {
        return new Reservation(null, member, date, reservationTime, theme);
    }
}
