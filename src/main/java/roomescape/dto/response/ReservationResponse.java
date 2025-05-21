package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationResponse(
        Long id,
        LocalDate date,
        String bookState,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberProfileResponse member
) {

    public ReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getDate(),
                reservation.getStatus().toString(),
                new ReservationTimeResponse(reservation.getReservationTime()),
                new ThemeResponse(reservation.getTheme()),
                new MemberProfileResponse(reservation.getMember())
        );
    }
}

