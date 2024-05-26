package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record ReservationWaitingResponse(
        long id,
        String name,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "kk:mm")
        LocalTime time
) {
    public static ReservationWaitingResponse from(final Reservation reservation) {
        return new ReservationWaitingResponse(
                reservation.getId(),
                reservation.getMember().getNameValue(),
                reservation.getTheme().getThemeNameValue(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
