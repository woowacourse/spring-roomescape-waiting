package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.TimeResponse;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public record BookingResponse(
        Long id,
        String status,
        String memberName,
        LocalDate date,
        TimeResponse time,
        Long themeId,
        String themeName
) {

    public static BookingResponse reserved(Reservation reservation) {
        return new BookingResponse(
                reservation.getId(),
                "RESERVED",
                reservation.getMember().getName(),
                reservation.getDate(),
                TimeResponse.of(reservation.getTime()),
                reservation.getTheme().getId(),
                reservation.getTheme().getName()
        );
    }

    public static BookingResponse waiting(ReservationWaiting waiting) {
        return new BookingResponse(
                waiting.getId(),
                "WAITING",
                waiting.getMember().getName(),
                waiting.getDate(),
                TimeResponse.of(waiting.getTime()),
                waiting.getTheme().getId(),
                waiting.getTheme().getName()
        );
    }
}
