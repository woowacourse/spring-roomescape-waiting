package roomescape.service.dto.response.wait;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.domain.Reservation;

public record AdminReservationResponse(String waitId,
                                       String memberName,
                                       String themeName,
                                       LocalDate date,
                                       LocalTime time) {

    public AdminReservationResponse(Reservation reservation) {
        this(
                reservation.getId().toString(),
                reservation.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt());
    }
}
