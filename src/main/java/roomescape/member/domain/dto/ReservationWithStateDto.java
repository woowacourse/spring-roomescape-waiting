package roomescape.member.domain.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;
import roomescape.theme.domain.dto.ThemeResponseDto;

public record ReservationWithStateDto(
        Long id,
        LocalDate date,
        String statusText,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme
) {

    public ReservationWithStateDto(Reservation reservation) {
        this(reservation.getId(),
                reservation.getDate(),
                reservation.getStatus().getDisplayName(),
                ReservationTimeResponseDto.of(reservation.getReservationTime()),
                ThemeResponseDto.of(reservation.getTheme())
        );
    }
}
