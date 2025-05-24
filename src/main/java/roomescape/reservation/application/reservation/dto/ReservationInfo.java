package roomescape.reservation.application.reservation.dto;

import java.time.LocalDate;
import roomescape.member.application.dto.MemberInfo;
import roomescape.reservation.application.theme.dto.ThemeInfo;
import roomescape.reservation.application.time.dto.ReservationTimeInfo;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationStatus;

public record ReservationInfo(
        long id,
        MemberInfo member,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme,
        String status
) {

    public ReservationInfo(final Reservation reservation) {
        this(
                reservation.id(),
                new MemberInfo(reservation.member()),
                reservation.date(),
                new ReservationTimeInfo(reservation.time()),
                new ThemeInfo(reservation.theme()),
                ReservationStatus.BOOKED.getDisplayName()
        );
    }
}
