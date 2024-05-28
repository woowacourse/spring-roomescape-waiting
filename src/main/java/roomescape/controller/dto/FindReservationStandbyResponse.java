package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;

public record FindReservationStandbyResponse(Long id,
                                             String name,
                                             String theme,
                                             LocalDate date,
                                             @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public static FindReservationStandbyResponse from(Reservation reservation) {
        return new FindReservationStandbyResponse(
            reservation.getId(),
            reservation.getMember().getName(),
            reservation.getTheme().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt()
        );
    }
}
