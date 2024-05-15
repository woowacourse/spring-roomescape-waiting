package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;

public record ReservationResponse(
        Long id,
        LocalDate date,
        MemberResponse member,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                MemberResponse.from(reservation.getMember()),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
