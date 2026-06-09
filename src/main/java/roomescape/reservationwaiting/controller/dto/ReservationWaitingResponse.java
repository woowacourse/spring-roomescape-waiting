package roomescape.reservationwaiting.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservationwaiting.ReservationWaiting;

public record ReservationWaitingResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        Long themeId,
        Long timeId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime requestAt
) {
    public static ReservationWaitingResponse from(final ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getDate(),
                reservationWaiting.getThemeId(),
                reservationWaiting.getTimeId(),
                reservationWaiting.getRequestAt()
        );
    }
}
