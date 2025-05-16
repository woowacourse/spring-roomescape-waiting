package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        UserResponseDto user
) {

    public ReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getDate(),
                new ReservationTimeResponse(reservation.getReservationTime()),
                new ThemeResponse(reservation.getTheme()),
                new UserResponseDto(reservation.getUser())
        );
    }
}

