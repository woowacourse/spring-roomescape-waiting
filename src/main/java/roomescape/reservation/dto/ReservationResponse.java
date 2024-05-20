package roomescape.reservation.dto;

import roomescape.member.dto.MemberNameResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.dto.ThemeResponse;

import java.time.format.DateTimeFormatter;

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
                reservation.getMember().getName().name(),
                reservation.getDate(DateTimeFormatter.ISO_DATE),
                new ReservationTimeResponse(reservation.getReservationTime()),
                new ThemeResponse(reservation.getTheme()),
                new MemberNameResponse(reservation.getMember())
        );
    }
}
