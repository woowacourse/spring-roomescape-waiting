package roomescape.reservation.service.dto;

import java.time.LocalDate;
import roomescape.member.service.dto.MemberInfo;
import roomescape.reservation.domain.Reservation;

public record ReservationInfo(
        Long id,
        MemberInfo member,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme
) {

    public ReservationInfo(final Reservation reservation) {
        this(
                reservation.getId(),
                new MemberInfo(reservation.getMember()),
                reservation.getDate(),
                new ReservationTimeInfo(reservation.getTime()),
                new ThemeInfo(reservation.getTheme())
        );
    }
}
