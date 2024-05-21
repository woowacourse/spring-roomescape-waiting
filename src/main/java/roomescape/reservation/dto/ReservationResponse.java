package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ThemeResponse theme,
        TimeResponse time
) {

    public ReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                new MemberResponse(reservation.getMember()),
                reservation.getDate(),
                new ThemeResponse(reservation.getTheme()),
                new TimeResponse(reservation.getTime())
        );
    }
}
