package roomescape.reservation.dto;

import java.time.format.DateTimeFormatter;
import roomescape.member.dto.MemberNameResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        String name,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberNameResponse member
) {

    public ReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getLoginMember().getName().name(),
                reservation.getDate(DateTimeFormatter.ISO_DATE),
                new ReservationTimeResponse(reservation.getTime()),
                new ThemeResponse(reservation.getTheme()),
                new MemberNameResponse(reservation.getLoginMember())
        );
    }
}
