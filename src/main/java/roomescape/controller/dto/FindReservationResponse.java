package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;

public record FindReservationResponse(Long id,
                                      String memberName,
                                      LocalDate date,
                                      @JsonFormat(pattern = "HH:mm") LocalTime time,
                                      String themeName) {

    public static FindReservationResponse from(Reservation reservation) {
        return new FindReservationResponse(
            reservation.getId(),
            reservation.getLoginMember().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            reservation.getTheme().getName()
        );
    }
}
