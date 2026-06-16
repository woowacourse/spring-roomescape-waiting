package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.TimeResponse;

public record ReservationResponse(
        Long id,
        String memberName,
        LocalDate date,
        TimeResponse time,
        Long themeId,
        String themeName,
        String status,
        Long price,
        String orderId
) {

    public static ReservationResponse from(Reservation reservation) {
        return of(reservation, null);
    }

    public static ReservationResponse of(Reservation reservation, String orderId) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                TimeResponse.of(reservation.getTime()),
                reservation.getTheme().getId(),
                reservation.getTheme().getName(),
                reservation.getStatus().name(),
                reservation.getTheme().getPrice(),
                orderId
        );
    }
}