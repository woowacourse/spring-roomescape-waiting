package roomescape.dto.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record MyReservationResponseDto(
        long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public MyReservationResponseDto(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }
}
