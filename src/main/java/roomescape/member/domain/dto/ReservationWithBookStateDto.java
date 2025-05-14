package roomescape.member.domain.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.domain.dto.ReservationTimeResponseDto;
import roomescape.theme.domain.dto.ThemeResponseDto;

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
