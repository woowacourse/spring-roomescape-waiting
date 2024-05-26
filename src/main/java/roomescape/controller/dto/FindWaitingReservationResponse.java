package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;

public record FindWaitingReservationResponse(
    Long id,
    String name,
    String theme,
    LocalDate date,
    @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {

    public static FindWaitingReservationResponse from(Reservation reservation) {
        return new FindWaitingReservationResponse(
            reservation.getId(),
            reservation.getName(),
            reservation.getTheme().getName(),
            reservation.getDate().getValue(),
            reservation.getTime().getStartAt()
        );
    }
}
