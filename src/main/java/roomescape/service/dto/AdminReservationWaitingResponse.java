package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.ReservationWaiting;

public record AdminReservationWaitingResponse(
        long id,
        String name,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {
    public static AdminReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        return new AdminReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getTheme().getName(),
                reservationWaiting.getMember().getName(),
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getStartAt()
        );
    }
}
