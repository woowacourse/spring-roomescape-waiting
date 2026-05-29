package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public record AdminReservationResponse(long id,
                                       String name,
                                       LocalDate date,
                                       String themeName,
                                       LocalTime time) {
    public static AdminReservationResponse from(Reservation reservation, Theme theme) {
        return new AdminReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                theme.getName(),
                reservation.getTime().getStartAt()
        );
    }
}
