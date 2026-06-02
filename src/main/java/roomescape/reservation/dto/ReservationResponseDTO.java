package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.ReservationTimeResponseDTO;
import roomescape.theme.dto.ThemeResponseDTO;

public record ReservationResponseDTO(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponseDTO time,
        ThemeResponseDTO theme
) {

    public static ReservationResponseDTO from(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponseDTO.from(
                        reservation.getTime()
                ),
                ThemeResponseDTO.from(reservation.getTheme())
        );
    }
}
