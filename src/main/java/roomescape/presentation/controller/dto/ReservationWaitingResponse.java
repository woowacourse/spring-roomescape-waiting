package roomescape.presentation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.application.dto.ReservationWaitingDto;

public record ReservationWaitingResponse(
        long reservationid,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {
    public static ReservationWaitingResponse from(ReservationWaitingDto reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.reservationId(),
                reservationWaiting.theme(),
                reservationWaiting.date(),
                reservationWaiting.time(),
                reservationWaiting.status()
        );
    }
}
