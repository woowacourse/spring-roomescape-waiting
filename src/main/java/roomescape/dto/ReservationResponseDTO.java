package roomescape.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;

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
                reservation.getReservationSlot().getDate(),
                ReservationTimeResponseDTO.from(
                        reservation.getReservationSlot().getTime()
                ),
                ThemeResponseDTO.from(reservation.getReservationSlot().getTheme())
        );
    }
}
