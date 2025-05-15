package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.member.application.dto.MemberInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

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
                reservation.getId(),
                new MemberInfo(reservation.getMember()),
                reservation.getDate(),
                new ReservationTimeInfo(reservation.getTime()),
                new ThemeInfo(reservation.getTheme()),
                reservation.getStatus()
        );
    }
}
