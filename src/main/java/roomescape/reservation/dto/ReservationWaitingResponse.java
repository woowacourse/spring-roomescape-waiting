package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record ReservationWaitingResponse(
        Long id,
        String memberName,
        String themeName,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time
) {

    public static ReservationWaitingResponse toResponse(Reservation reservation) {
        return new ReservationWaitingResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
