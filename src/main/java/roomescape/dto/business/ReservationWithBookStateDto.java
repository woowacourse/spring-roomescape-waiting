package roomescape.dto.business;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.dto.response.ThemeResponseDto;

public record ReservationWithBookStateDto(
        Long id,
        LocalDate date,
        String statusText,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme
) {

    public ReservationWithBookStateDto(Reservation reservation) {
        this(reservation.getId(),
                reservation.getDate(),
                reservation.getStatus().getDisplayName(),
                ReservationTimeResponseDto.of(reservation.getReservationTime()),
                ThemeResponseDto.of(reservation.getTheme())
        );
    }
}
