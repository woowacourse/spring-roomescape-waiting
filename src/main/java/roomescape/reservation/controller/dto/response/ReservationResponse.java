package roomescape.reservation.controller.dto.response;

import java.time.LocalDate;
import roomescape.member.controller.dto.response.MemberNameResponse;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        long id,
        MemberNameResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                new MemberNameResponse(reservation.getMember().getNameValue()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
