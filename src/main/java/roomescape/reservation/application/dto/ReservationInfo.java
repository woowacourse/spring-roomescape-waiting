package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.member.application.dto.MemberInfo;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationStatus;

public record ReservationInfo(
        Long id,
        MemberInfo member,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme,
        ReservationStatus status
) {

    public ReservationInfo(final Reservation reservation) {
        this(
                reservation.id(),
                new MemberInfo(reservation.member()),
                reservation.date(),
                new ReservationTimeInfo(reservation.time()),
                new ThemeInfo(reservation.theme()),
                reservation.status()
        );
    }
}
